package com.zulian.ayudafinanzas.data

data class ChequeDetalle(
    val sucursal: Int,
    val numeroCuenta: Long,
    val causal: String
)