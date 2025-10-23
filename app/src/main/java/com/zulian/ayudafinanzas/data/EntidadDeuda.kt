package com.zulian.ayudafinanzas.data

data class EntidadDeuda(
    val entidad: String,
    val situacion: Int,
    val fechaSit1: String?,
    val monto: Double,
    val diasAtrasoPago: Int,
    val refinanciaciones: Boolean,
    val recategorizacionOblig: Boolean,
    val situacionJuridica: Boolean,
    val irrecDisposicionTecnica: Boolean,
    val enRevision: Boolean,
    val procesoJud: Boolean
)