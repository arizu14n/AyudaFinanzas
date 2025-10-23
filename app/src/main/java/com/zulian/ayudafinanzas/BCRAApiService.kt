package com.zulian.ayudafinanzas

import com.zulian.ayudafinanzas.data.ChequeEntidadResponse
import com.zulian.ayudafinanzas.data.ChequeResponse
import com.zulian.ayudafinanzas.data.CotizacionesResponse
import com.zulian.ayudafinanzas.data.DeudaResponse
import com.zulian.ayudafinanzas.data.DivisaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BCRAApiService {

    // Endpoints de Estadísticas Cambiarias
    @GET("estadisticascambiarias/v1.0/Maestros/Divisas")
    fun getDivisas(): Call<DivisaResponse>

    @GET("estadisticascambiarias/v1.0/Cotizaciones")
    fun getCotizaciones(
        @Query("fecha") fecha: String
    ): Call<CotizacionesResponse>

    @GET("estadisticascambiarias/v1.0/Cotizaciones/{codMoneda}")
    fun getCotizacionesPorDivisa(
        @Path("codMoneda") codMoneda: String,
        @Query("fechaDesde") fechaDesde: String,
        @Query("fechaHasta") fechaHasta: String
    ): Call<CotizacionesResponse>

    // Endpoint de Central de Deudores
    @GET("centraldedeudores/v1.0/Deudas/{Identificacion}")
    fun getDeudas(
        @Path("Identificacion") identificacion: Long
    ): Call<DeudaResponse>

    // Endpoints de Cheques Denunciados
    @GET("cheques/v1.0/entidades")
    fun getChequeEntidades(): Call<ChequeEntidadResponse>

    @GET("cheques/v1.0/denunciados/{codigoEntidad}/{numeroCheque}")
    fun getChequeDenunciado(
        @Path("codigoEntidad") codigoEntidad: Int,
        @Path("numeroCheque") numeroCheque: Long
    ): Call<ChequeResponse>
}