package com.idloquy.landmark.data.repository.exceptions

data class InvalidGroupIdException(
    override val message: String,
) : Exception(message)