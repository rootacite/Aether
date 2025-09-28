
package com.acitelight.aether.service

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.acitelight.aether.AetherApp
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.ConnectionSpec
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.EventListener
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayInputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Singleton
class ApiClient @Inject constructor(

) {
    fun getBase(): String{
        return replaceAbyssProtocol(base)
    }
    private var base: String = ""
    private var domain: String = ""
    private var cert: String = ""
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private fun replaceAbyssProtocol(uri: String): String {
        return uri.replaceFirst("^abyss://".toRegex(), "https://")
    }

    private val dnsEventListener = object : EventListener() {
        override fun dnsEnd(call: okhttp3.Call, domainName: String, inetAddressList: List<InetAddress>) {
            super.dnsEnd(call, domainName, inetAddressList)
            val ipAddresses = inetAddressList.joinToString(", ") { it.hostAddress ?: "" }
            Log.d("OkHttp_DNS", "Domain '$domainName' resolved to IPs: [$ipAddresses]")
        }
    }

    private fun loadCertificateFromString(pemString: String): X509Certificate {
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

    private fun createOkHttpClientWithDynamicCert(trustedCert: X509Certificate?): OkHttpClient {
        try {
            val defaultTmFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            ).apply {
                init(null as KeyStore?)
            }
            val defaultTm = defaultTmFactory.trustManagers
                .first { it is X509TrustManager } as X509TrustManager

            val customTm: X509TrustManager? = trustedCert?.let {
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                    load(null, null)
                    setCertificateEntry("ca", it)
                }
                val tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
                ).apply {
                    init(keyStore)
                }
                tmf.trustManagers.first { i -> i is X509TrustManager } as X509TrustManager
            }

            val combinedTm = object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return (defaultTm.acceptedIssuers + (customTm?.acceptedIssuers ?: emptyArray()))
                }

                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                    var passed = false
                    try {
                        defaultTm.checkClientTrusted(chain, authType)
                        passed = true
                    } catch (_: CertificateException) { }
                    if (!passed && customTm != null) {
                        customTm.checkClientTrusted(chain, authType)
                        passed = true
                    }
                    if (!passed) throw CertificateException("Untrusted client certificate chain")
                }

                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                    var passed = false
                    try {
                        defaultTm.checkServerTrusted(chain, authType)
                        passed = true
                    } catch (_: CertificateException) { }
                    if (!passed && customTm != null) {
                        customTm.checkServerTrusted(chain, authType)
                        passed = true
                    }
                    if (!passed) throw CertificateException("Untrusted server certificate chain")
                }
            }

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf(combinedTm), null)
            }

            return if (base.startsWith("abyss://"))
                OkHttpClient.Builder()
                    .connectionSpecs(
                        listOf(
                            ConnectionSpec.MODERN_TLS,
                            ConnectionSpec.COMPATIBLE_TLS
                        )
                    )
                    .proxy(
                        Proxy(
                            Proxy.Type.HTTP,
                            InetSocketAddress("::1", 4095)
                        )
                    )
                    .sslSocketFactory(sslContext.socketFactory, combinedTm)
                    .build()
            else
                OkHttpClient.Builder()
                    .connectionSpecs(
                        listOf(
                            ConnectionSpec.MODERN_TLS,
                            ConnectionSpec.COMPATIBLE_TLS
                        )
                    )
                    .sslSocketFactory(sslContext.socketFactory, combinedTm)
                    .build()

        } catch (e: Exception) {
            throw RuntimeException("Failed to create OkHttpClient with dynamic certificate", e)
        }
    }

    private fun createOkHttp(): OkHttpClient {
        return if (cert == "")
            if (base.startsWith("abyss://"))
                OkHttpClient
                    .Builder()
                    .cookieJar(object : CookieJar {
                        private val cookieStore = mutableMapOf<HttpUrl, List<Cookie>>()

                        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                            cookieStore[url] = cookies
                        }

                        override fun loadForRequest(url: HttpUrl): List<Cookie> {
                            return cookieStore[url] ?: emptyList()
                        }
                    })
                    .proxy(
                        Proxy(
                            Proxy.Type.HTTP,
                            InetSocketAddress("::1", 4095)
                        )
                    )
                    .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                    .eventListener(dnsEventListener)
                    .build()
            else
                OkHttpClient
                    .Builder()
                    .cookieJar(object : CookieJar {
                        private val cookieStore = mutableMapOf<HttpUrl, List<Cookie>>()

                        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                            cookieStore[url] = cookies
                        }

                        override fun loadForRequest(url: HttpUrl): List<Cookie> {
                            return cookieStore[url] ?: emptyList()
                        }
                    })
                    .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                    .eventListener(dnsEventListener)
                    .build()
        else
            createOkHttpClientWithDynamicCert(loadCertificateFromString(cert))

    }

    private fun createRetrofit(): Retrofit {
        client = createOkHttp()
        val b = replaceAbyssProtocol(base)

        return Retrofit.Builder()
            .baseUrl(b)
            .client(client!!)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private var client: OkHttpClient? = null
    var api: ApiInterface? = null

    fun getClient() = client!!

    suspend fun apply(context: Context, urls: String, crt: String): String? {
        try {
            val urlList = urls.split(";").map { it.trim() }

            var selectedUrl: String? = null
            for (url in urlList) {
                val host = url.toUri().host
                if (host != null && pingHost(host)) {
                    selectedUrl = url
                    break
                }
            }

            if (selectedUrl == null) {
                throw Exception("No reachable URL found")
            }

            domain = selectedUrl.toHttpUrlOrNull()?.host ?: ""
            cert = crt
            base = selectedUrl
            withContext(Dispatchers.IO)
            {
                (context as AetherApp).abyssService?.proxy?.config(getBase().toUri().host!!, 4096)
            }
            api = createRetrofit().create(ApiInterface::class.java)

            Log.i("Delay Analyze", "Start Abyss Hello")
            val h = api!!.hello()
            Log.i("Delay Analyze", "Abyss Hello: ${h.string()}")

            return base
        } catch (_: Exception) {
            api = null
            base = ""
            domain = ""
            cert = ""
            return null
        }
    }

    private suspend fun pingHost(host: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val address = InetAddress.getByName(host)
            address.isReachable(200)
        } catch (_: Exception) {
            false
        }
    }
}