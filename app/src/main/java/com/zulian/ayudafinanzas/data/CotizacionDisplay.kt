package com.zulian.ayudafinanzas.data

// Clase simple para contener los datos que realmente vamos a mostrar en la UI
data class CotizacionDisplay(
    val fecha: String, // Formato DD/MM
    val descripcion: String,
    val tipoCotizacion: Double
)