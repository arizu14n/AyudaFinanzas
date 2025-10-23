package com.zulian.ayudafinanzas.data

data class ChequeResult(
    val numeroCheque: Long,
    val denunciado: Boolean,
    val fechaProcesamiento: String,
    val denominacionEntidad: String,
    val detalles: List<ChequeDetalle>
)