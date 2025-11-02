package com.zulian.ayudafinanzas.data

import com.google.gson.annotations.SerializedName

data class DolarRate(
    @SerializedName("moneda")
    val moneda: String,

    @SerializedName("casa")
    val casa: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("compra")
    val compra: Double,

    @SerializedName("venta")
    val venta: Double,

    @SerializedName("fechaActualizacion")
    val fechaActualizacion: String
)
