package com.zulian.ayudafinanzas.data.check

import com.google.gson.annotations.SerializedName

data class Check(
    val id: Int,
    val nro: String,
    val banco: String,
    val librador: String,
    @SerializedName("fecha_emision") val fechaEmision: String,
    val importe: String,
    @SerializedName("imagen_url") val imagenUrl: String,
    val estado: String,
    @SerializedName("usuario_id") val usuarioId: Int
)

data class CheckResponse(
    val message: String,
    val check: Check
)