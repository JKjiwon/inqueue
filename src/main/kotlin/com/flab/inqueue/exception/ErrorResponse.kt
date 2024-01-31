package com.flab.inqueue.exception

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class ErrorResponse(
    val code: Int,
    val message: String,
    val fieldError: List<FieldError>? = null
) {
    data class FieldError(
        val field: String,
        val value: Any?,
        val reason: String,
    )
}