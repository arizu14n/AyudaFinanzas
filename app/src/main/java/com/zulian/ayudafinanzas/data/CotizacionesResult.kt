package com.zulian.ayudafinanzas.data

data class CotizacionesResult(
    val fecha: String?, // <-- CAMBIO: Hacemos la fecha nullable
    val detalle: List<DetalleCotizacion>
)