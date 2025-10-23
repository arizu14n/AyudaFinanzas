package com.zulian.ayudafinanzas.data

data class DivisaResponse(
    val status: Int,
    val results: List<Divisa>
)