package com.zulian.ayudafinanzas.data

// El campo "results" puede ser un objeto o una lista, así que lo declaramos como Any
// y lo procesaremos manualmente después.
data class CotizacionesResponse(
    val status: Int,
    val results: Any
)