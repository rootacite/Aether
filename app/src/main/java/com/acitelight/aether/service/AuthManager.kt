package com.acitelight.aether.service

import android.util.Base64
import com.acitelight.aether.model.ChallengeResponse
import kotlinx.coroutines.runBlocking
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.PrivateKey
import java.security.Signature

object AuthManager {
    suspend fun fetchToken(username: String, privateKey: String): String? {
        val api = ApiClient.api
        var challengeBase64 = ""

        try{
            challengeBase64 = api!!.getChallenge(username).string()
        }catch (e: Exception)
        {
            print(e.message)
        }

        val signedBase64 = signChallenge(db64(privateKey), db64(challengeBase64))

        return try {
            api!!.verifyChallenge(username, ChallengeResponse(response = signedBase64)).string()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun db64(b64: String): ByteArray {
        return Base64.decode(b64, Base64.DEFAULT) // 32 bytes
    }

    fun signChallenge(privateKey: ByteArray, data: ByteArray): String
    {
        val privateKeyParams = Ed25519PrivateKeyParameters(privateKey, 0)
        val signer = Ed25519Signer()
        signer.init(true, privateKeyParams)

        signer.update(data, 0, data.size)
        val signature = signer.generateSignature()
        return Base64.encodeToString(signature, Base64.NO_WRAP)
    }
}