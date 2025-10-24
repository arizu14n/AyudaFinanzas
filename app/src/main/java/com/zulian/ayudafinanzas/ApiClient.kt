package com.zulian.ayudafinanzas

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "https://api.bcra.gob.ar/"

    // Volvemos a un cliente OkHttp estándar. 
    // La configuración de seguridad ahora se maneja en network_security_config.xml
    private val httpClient = OkHttpClient.Builder().build()

    val apiService: BCRAApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BCRAApiService::class.java)
    }
}