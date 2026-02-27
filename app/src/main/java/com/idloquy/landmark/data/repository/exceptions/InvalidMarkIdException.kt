package com.idloquy.landmark.data.repository.exceptions

data class InvalidMarkIdException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
