package com.zulian.ayudafinanzas

import com.zulian.ayudafinanzas.data.DolarRate
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface DolarApiService {
    @GET("v1/dolares")
    fun getAllDolarRates(): Call<List<DolarRate>>
}

object DolarApiClient {
    private const val BASE_URL = "https://dolarapi.com/"

    val apiService: DolarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DolarApiService::class.java)
    }
}
