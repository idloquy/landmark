package com.idloquy.landmark.ui.shared_marks.exceptions

data class InvalidServerResponseException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)