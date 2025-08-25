
package com.acitelight.aether.service

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object ApiClient {
    var base: String = ""
    var domain: String = ""
    var cert: String = ""
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun loadCertificateFromString(pemString: String): X509Certificate {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val decodedPem = pemString
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replace("\\s+".toRegex(), "")

        val decodedBytes = android.util.Base64.decode(decodedPem, android.util.Base64.DEFAULT)

        ByteArrayInputStream(decodedBytes).use { inputStream ->
            return certificateFactory.generateCertificate(inputStream) as X509Certificate
        }
    }

    fun createOkHttpClientWithDynamicCert(trustedCert: X509Certificate): OkHttpClient {
        try {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(null, null)
                setCertificateEntry("ca", trustedCert)
            }

            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
                init(keyStore)
            }

            val trustManager = tmf.trustManagers.first { it is X509TrustManager } as X509TrustManager

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf(trustManager), null)
            }

            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager)
                .build()

        } catch (e: Exception) {
            throw RuntimeException("Failed to create OkHttpClient with dynamic certificate", e)
        }
    }

    private fun createRetrofit(): Retrofit {
        val okHttpClient = createOkHttpClientWithDynamicCert(loadCertificateFromString(cert))

        return Retrofit.Builder()
            .baseUrl(base)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }


    var api: ApiInterface? = null

    fun apply(url: String, crt: String)
    {
        try{
            domain = url.toHttpUrlOrNull()?.host !!
            cert = crt
            base = url
            api = createRetrofit().create(ApiInterface::class.java)
        }catch (e: Exception)
        {
            api = null
            base = ""
            domain = ""
            cert = ""
        }
    }
}