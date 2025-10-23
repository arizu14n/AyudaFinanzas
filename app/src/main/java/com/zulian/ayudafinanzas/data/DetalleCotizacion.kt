package com.zulian.ayudafinanzas.data

data class DetalleCotizacion(
    val codigoMoneda: String,
    val descripcion: String,
    val tipoPase: Double, // CAMBIADO de Int a Double
    val tipoCotizacion: Double // CAMBIADO de Int a Double
)