package com.zulian.ayudafinanzas

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {

    private const val BASE_URL = "https://api.bcra.gob.ar/"

    // ADVERTENCIA: El siguiente código es INSEGURO y solo para desarrollo.
    // Se usa para confiar en todos los certificados SSL, incluyendo los no válidos.
    // Esto soluciona el error "CertPathValidatorException" pero expone la app a ataques.
    // NO USAR EN PRODUCCIÓN.
    private fun createUnsafeOkHttpClient(): OkHttpClient.Builder {
        try {
            // Crea un trust manager que no valida las cadenas de certificados
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Instala el trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            // Crea un socket factory con nuestro trust manager
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            return builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    val apiService: BCRAApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createUnsafeOkHttpClient().build()) // Usa el cliente OkHttp inseguro
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BCRAApiService::class.java)
    }
}