package com.zulian.ayudafinanzas.data

data class DeudaResponse(
    val status: Int,
    val results: DeudaResult? // Hacemos nullable para manejar el caso de CUIT no encontrado
)