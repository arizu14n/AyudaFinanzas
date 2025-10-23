package com.zulian.ayudafinanzas.data

data class ErrorResponse(
    val status: Int,
    val errorMessages: List<String>
)