package com.zulian.ayudafinanzas.data

data class ChequeResponse(
    val status: Int,
    val results: ChequeResult? // Nullable para manejar cheques no encontrados
)