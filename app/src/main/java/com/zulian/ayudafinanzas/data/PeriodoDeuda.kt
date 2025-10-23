package com.zulian.ayudafinanzas.data

data class PeriodoDeuda(
    val periodo: String,
    val entidades: List<EntidadDeuda>
)