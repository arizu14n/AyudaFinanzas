package com.zulian.ayudafinanzas

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface BNAApiService {
    @GET("Cotizador/HistoricoPrincipales")
    fun getHistoricoCotizaciones(
        @Query("id") id: String = "billetes",
        @Query("fecha") fecha: String,
        @Query("filtroEuro") filtroEuro: Int = 1,
        @Query("filtroDolar") filtroDolar: Int = 1
    ): Call<ResponseBody>
}

object BNAApiClient {
    private const val BASE_URL = "https://www.bna.com.ar/"

    val apiService: BNAApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(BNAApiService::class.java)
    }
}
