package com.zulian.ayudafinanzas.data

data class DeudaResult(
    val identificacion: Long,
    val denominacion: String,
    val periodos: List<PeriodoDeuda>
)