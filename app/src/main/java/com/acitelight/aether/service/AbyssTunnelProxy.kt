package com.acitelight.aether.service


import android.util.Log
import com.acitelight.aether.service.AuthManager.db64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.select
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class AbyssTunnelProxy @Inject constructor(
    private val settingsDataStoreManager: SettingsDataStoreManager
) {
    private val coroutineContext: CoroutineContext = Dispatchers.IO
    private var serverHost: String = ""
    private var serverPort: Int = 0

    fun config(host: String, port: Int)
    {
        serverHost = host
        serverPort = port
    }

    private val listenAddress = InetAddress.getLoopbackAddress()
    private val listenPort = 4095
    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + coroutineContext)

    fun start() {
        serverSocket = ServerSocket(listenPort, 50, listenAddress)
        // accept loop
        scope.launch {
            val srv = serverSocket ?: return@launch
            try {
                while (true) {
                    val client = srv.accept()

                    if(serverHost.isEmpty())
                        continue

                    launch {
                        try { handleLocalConnection(client) }
                        catch (ex: Exception) { /* ignore */ }
                    }
                }
            } catch (ex: Exception) {
                println(ex.message)
                // server stopped or fatal error
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        try { serverSocket?.close() } catch (_: Exception) {}
        scope.cancel()
    }

    private suspend fun handleLocalConnection(localSocket: Socket) = withContext(coroutineContext) {
        val localIn = localSocket.getInputStream()
        val localOut = localSocket.getOutputStream()
        var abyssSocket: Socket? = null
        var abyssStream: AbyssStream? = null
        try {
            abyssSocket = Socket(serverHost, serverPort)
            abyssStream = AbyssStream.create(abyssSocket, db64(settingsDataStoreManager.privateKeyFlow.first()))

            // concurrently copy in both directions
            val job1 = launch { copyExactSuspend(localIn, abyssStream) }   // local -> abyss
            val job2 = launch { copyFromAbyssToLocal(abyssStream, localOut) } // abyss -> local

            // wait for either direction to finish
            select<Unit> {
                job1.onJoin { /* completed */ }
                job2.onJoin { /* completed */ }
            }
            // cancel other
            job1.cancel()
            job2.cancel()
        } catch (ex: Exception)
        {
            println(ex.message)
            // log or ignore; we close sockets below
        } finally {
            try { localSocket.close() } catch (_: Exception) {}
            try { abyssStream?.close() } catch (_: Exception) {}
            try { abyssSocket?.close() } catch (_: Exception) {}
        }
        return@withContext
    }

    // Copy from local InputStream into AbyssStream.write in frames.
    private suspend fun copyExactSuspend(localIn: InputStream, abyss: AbyssStream) = withContext(coroutineContext) {
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = localIn.read(buffer)
            if (read <= 0)
                break

            abyss.write(buffer, 0, read)
        }
    }

    // Copy from AbyssStream (read frames/decrypt) to local OutputStream
    private suspend fun copyFromAbyssToLocal(abyss: AbyssStream, localOut: OutputStream) = withContext(coroutineContext) {
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val n = abyss.read(buffer, 0, buffer.size)
            if (n <= 0)
                break
            localOut.write(buffer, 0, n)
        }
    }
}