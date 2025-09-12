package com.acitelight.aether.service

import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream

import java.net.Socket
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.ArrayDeque

import org.bouncycastle.math.ec.rfc7748.X25519
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import java.io.EOFException
import java.util.concurrent.atomic.AtomicLong

class AbyssStream private constructor(
    private val socket: Socket,
    private val input: InputStream,
    private val output: OutputStream,
    private val aeadKey: ByteArray,
    private val sendSalt: ByteArray,
    private val recvSalt: ByteArray
) {
    companion object {
        private const val PUBLIC_KEY_LEN = 32
        private const val AEAD_KEY_LEN = 32
        private const val NONCE_SALT_LEN = 4
        private const val AEAD_TAG_LEN = 16
        private const val NONCE_LEN = 12
        private const val MAX_PLAINTEXT_FRAME = 64 * 1024

        private val secureRandom = SecureRandom()

        /**
         * Create and perform handshake on an already-connected socket.
         * If privateKeyRaw is provided, it must be 32 bytes.
         */
        suspend fun create(socket: Socket, privateKeyRaw: ByteArray? = null): AbyssStream = withContext(Dispatchers.IO) {
            if (!socket.isConnected) throw IllegalArgumentException("socket is not connected")
            val inStream = socket.getInputStream()
            val outStream = socket.getOutputStream()

            // 1) keypair (raw)
            val localPriv = ByteArray(PUBLIC_KEY_LEN)
            if (privateKeyRaw != null) {
                if (privateKeyRaw.size != PUBLIC_KEY_LEN) {
                    throw IllegalArgumentException("privateKeyRaw must be $PUBLIC_KEY_LEN bytes")
                }
                System.arraycopy(privateKeyRaw, 0, localPriv, 0, PUBLIC_KEY_LEN)
            } else {
                X25519.generatePrivateKey(secureRandom, localPriv)
            }
            val localPub = ByteArray(PUBLIC_KEY_LEN)
            X25519.scalarMultBase(localPriv, 0, localPub, 0)

            // 2) exchange raw public keys (exact 32 bytes each) using blocking IO
            writeExact(outStream, localPub, 0, PUBLIC_KEY_LEN)
            val remotePub = ByteArray(PUBLIC_KEY_LEN)
            readExact(inStream, remotePub, 0, PUBLIC_KEY_LEN)

            // 3) compute shared secret: X25519.scalarMult(private, remotePublic)
            val shared = ByteArray(PUBLIC_KEY_LEN)
            X25519.scalarMult(localPriv, 0, remotePub, 0, shared, 0)

            // 4) HKDF-SHA256 -> AEAD key + saltA + saltB
            val hkdf = HKDFBytesGenerator(SHA256Digest())
            // AEAD key
            hkdf.init(HKDFParameters(shared, null, "Abyss-AEAD-Key".toByteArray(Charsets.US_ASCII)))
            val aeadKey = ByteArray(AEAD_KEY_LEN)
            hkdf.generateBytes(aeadKey, 0, AEAD_KEY_LEN)

            // salt A
            hkdf.init(HKDFParameters(shared, null, "Abyss-Nonce-Salt-A".toByteArray(Charsets.US_ASCII)))
            val saltA = ByteArray(NONCE_SALT_LEN)
            hkdf.generateBytes(saltA, 0, NONCE_SALT_LEN)

            // salt B
            hkdf.init(HKDFParameters(shared, null, "Abyss-Nonce-Salt-B".toByteArray(Charsets.US_ASCII)))
            val saltB = ByteArray(NONCE_SALT_LEN)
            hkdf.generateBytes(saltB, 0, NONCE_SALT_LEN)

            // Deterministic assignment by lexicographic comparison
            val cmp = lexicographicCompare(localPub, remotePub)
            val sendSalt: ByteArray
            val recvSalt: ByteArray
            if (cmp < 0) {
                sendSalt = saltA
                recvSalt = saltB
            } else if (cmp > 0) {
                sendSalt = saltB
                recvSalt = saltA
            } else {
                // extremely unlikely
                sendSalt = saltA
                recvSalt = saltB
            }

            // zero sensitive buffers
            localPriv.fill(0)
            localPub.fill(0)
            remotePub.fill(0)
            shared.fill(0)
            // keep aeadKey, sendSalt, recvSalt

            return@withContext AbyssStream(socket, inStream, outStream, aeadKey, sendSalt, recvSalt)
        }

        private fun lexicographicCompare(a: ByteArray, b: ByteArray): Int {
            val min = kotlin.math.min(a.size, b.size)
            for (i in 0 until min) {
                val av = a[i].toInt() and 0xff
                val bv = b[i].toInt() and 0xff
                if (av < bv) return -1
                if (av > bv) return 1
            }
            if (a.size < b.size) return -1
            if (a.size > b.size) return 1
            return 0
        }

        private fun readExact(input: InputStream, buffer: ByteArray, offset: Int, count: Int) {
            var read = 0
            while (read < count) {
                val n = input.read(buffer, offset + read, count - read)
                if (n == -1) {
                    if (read == 0) throw EOFException("Remote closed connection while reading")
                    else throw EOFException("Remote closed connection unexpectedly during read")
                }
                read += n
            }
        }

        private fun writeExact(output: OutputStream, buffer: ByteArray, offset: Int, count: Int) {
            output.write(buffer, offset, count)
            output.flush()
        }
    }

    // internal state
    private val sendCounter = AtomicLong(0L)
    private val recvCounter = AtomicLong(0L)
    private val sendLock = Any()
    private val aeadLock = Any()

    // leftover read queue
    private val leftoverQueue = ArrayDeque<ByteArray>()
    private var currentLeftover: ByteArray? = null
    private var currentLeftoverOffset = 0

    @Volatile
    private var closed = false

    // ---- high-level read/write APIs (suspendable) ----

    suspend fun read(buffer: ByteArray, offset: Int, count: Int): Int = withContext(Dispatchers.IO) {
        if (closed) throw IllegalStateException("AbyssStream closed")
        if (buffer.size < offset + count) throw IndexOutOfBoundsException()
        // serve leftover first
        if (ensureCurrentLeftover()) {
            val seg = currentLeftover!!
            val avail = seg.size - currentLeftoverOffset
            val toCopy = kotlin.math.min(avail, count)
            System.arraycopy(seg, currentLeftoverOffset, buffer, offset, toCopy)
            currentLeftoverOffset += toCopy
            if (currentLeftoverOffset >= seg.size) {
                currentLeftover = null
                currentLeftoverOffset = 0
            }
            return@withContext toCopy
        }

        // read one frame and decrypt
        val plaintext = readOneFrameAndDecrypt()
        if (plaintext == null || plaintext.isEmpty()) {
            // EOF
            return@withContext 0
        }

        return@withContext if (plaintext.size <= count) {
            System.arraycopy(plaintext, 0, buffer, offset, plaintext.size)
            plaintext.size
        } else {
            System.arraycopy(plaintext, 0, buffer, offset, count)
            val leftoverLen = plaintext.size - count
            val leftover = ByteArray(leftoverLen)
            System.arraycopy(plaintext, count, leftover, 0, leftoverLen)
            synchronized(leftoverQueue) { leftoverQueue.addLast(leftover) }
            count
        }
    }

    private fun ensureCurrentLeftover(): Boolean {
        if (currentLeftover != null && currentLeftoverOffset < currentLeftover!!.size) return true
        synchronized(leftoverQueue) {
            val next = leftoverQueue.pollFirst()
            if (next != null) {
                currentLeftover = next
                currentLeftoverOffset = 0
                return true
            }
        }
        return false
    }

    private fun readOneFrameAndDecrypt(): ByteArray? {
        // read 4-byte header
        val header = ByteArray(4)
        try {
            readExact(input, header, 0, 4)
        } catch (e: EOFException) {
            return null
        }
        val payloadLen = ByteBuffer.wrap(header).int and 0xffffffff.toInt()
        if (payloadLen > MAX_PLAINTEXT_FRAME + AEAD_TAG_LEN) throw IllegalStateException("payload too big")
        if (payloadLen < AEAD_TAG_LEN) throw IllegalStateException("payload too small")

        val payload = ByteArray(payloadLen)
        readExact(input, payload, 0, payloadLen)

        val ciphertextLen = payloadLen - AEAD_TAG_LEN
        val ciphertext = ByteArray(ciphertextLen)
        val tag = ByteArray(AEAD_TAG_LEN)
        if (ciphertextLen > 0) System.arraycopy(payload, 0, ciphertext, 0, ciphertextLen)
        System.arraycopy(payload, ciphertextLen, tag, 0, AEAD_TAG_LEN)

        val remoteCounterValue = recvCounter.getAndIncrement()

        val nonce = ByteArray(NONCE_LEN)
        System.arraycopy(recvSalt, 0, nonce, 0, NONCE_SALT_LEN)
        // write 8-byte big-endian counter at nonce[4..11]
        val bb = ByteBuffer.wrap(nonce, NONCE_SALT_LEN, 8)
        bb.putLong(remoteCounterValue)

        val plaintext = try {
            aeadDecrypt(nonce, ciphertext, tag)
        } catch (ex: Exception) {
            close()
            throw SecurityException("AEAD authentication failed; connection closed.", ex)
        } finally {
            nonce.fill(0)
            payload.fill(0)
            ciphertext.fill(0)
            tag.fill(0)
        }

        return plaintext
    }

    suspend fun write(buffer: ByteArray, offset: Int, count: Int) = withContext(Dispatchers.IO) {
        if (closed) throw IllegalStateException("AbyssStream closed")
        if (buffer.size < offset + count) throw IndexOutOfBoundsException()
        var remaining = count
        var idx = offset
        while (remaining > 0) {
            val chunk = kotlin.math.min(remaining, MAX_PLAINTEXT_FRAME)
            val plaintext = buffer.copyOfRange(idx, idx + chunk)
            sendPlaintextChunk(plaintext)
            idx += chunk
            remaining -= chunk
        }
    }

    private fun sendPlaintextChunk(plaintext: ByteArray) {
        if (closed) throw IllegalStateException("AbyssStream closed")

        val ciphertextAndTag: ByteArray
        val nonce = ByteArray(NONCE_LEN)
        val counterValue: Long
        synchronized(sendLock) {
            counterValue = sendCounter.getAndIncrement()
        }
        System.arraycopy(sendSalt, 0, nonce, 0, NONCE_SALT_LEN)
        val bb = ByteBuffer.wrap(nonce, NONCE_SALT_LEN, 8)
        bb.putLong(counterValue)

        try {
            ciphertextAndTag = aeadEncrypt(nonce, plaintext)
        } finally {
            nonce.fill(0)
        }

        val payloadLen = ciphertextAndTag.size
        val header = ByteBuffer.allocate(4).putInt(payloadLen).array()
        try {
            synchronized(output) {
                output.write(header)
                if (payloadLen > 0) output.write(ciphertextAndTag)
                output.flush()
            }
        } finally {
            // clear sensitive
            ciphertextAndTag.fill(0)
            plaintext.fill(0)
        }
    }

    // ---- AEAD helpers using BouncyCastle lightweight API ----
    // ChaCha20-Poly1305 with 12-byte nonce. BouncyCastle ChaCha20Poly1305 produces ciphertext+tag.

    private fun aeadEncrypt(nonce: ByteArray, plaintext: ByteArray): ByteArray {
        synchronized(aeadLock) {
            val cipher = ChaCha20Poly1305()
            val params = AEADParameters(KeyParameter(aeadKey), AEAD_TAG_LEN * 8, nonce, null)
            cipher.init(true, params)
            val outBuf = ByteArray(cipher.getOutputSize(plaintext.size))
            var len = cipher.processBytes(plaintext, 0, plaintext.size, outBuf, 0)
            len += cipher.doFinal(outBuf, len)
            if (len != outBuf.size) return outBuf.copyOf(len)
            return outBuf
        }
    }

    private fun aeadDecrypt(nonce: ByteArray, ciphertext: ByteArray, tag: ByteArray): ByteArray {
        synchronized(aeadLock) {
            val cipher = ChaCha20Poly1305()
            val params = AEADParameters(KeyParameter(aeadKey), AEAD_TAG_LEN * 8, nonce, null)
            cipher.init(false, params)
            // input is ciphertext||tag
            val input = ByteArray(ciphertext.size + tag.size)
            if (ciphertext.isNotEmpty()) System.arraycopy(ciphertext, 0, input, 0, ciphertext.size)
            System.arraycopy(tag, 0, input, ciphertext.size, tag.size)
            val outBuf = ByteArray(cipher.getOutputSize(input.size))
            var len = cipher.processBytes(input, 0, input.size, outBuf, 0)
            try {
                len += cipher.doFinal(outBuf, len)
            } catch (ex: Exception) {
                // authentication failure or other
                throw ex
            }
            return if (len != outBuf.size) outBuf.copyOf(len) else outBuf
        }
    }

    // ---- utility / lifecycle ----

    fun close() {
        if (!closed) {
            closed = true
            try { socket.close() } catch (_: Exception) {}
            // clear secrets
            aeadKey.fill(0)
            sendSalt.fill(0)
            recvSalt.fill(0)
            synchronized(leftoverQueue) {
                leftoverQueue.forEach { it.fill(0) }
                leftoverQueue.clear()
            }
            currentLeftover = null
        }
    }
}