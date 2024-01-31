package com.flab.inqueue.exception

import org.springframework.http.HttpStatus

class ApplicationException(
        val httpStatus: HttpStatus,
        val code: Int,
        message: String,
        throwable: Throwable? = null
) : RuntimeException(message, throwable) {

    companion object {
        fun of(errorCode: ErrorCode): ApplicationException {
            return ApplicationException(errorCode.httpStatus, errorCode.code, errorCode.message)
        }
    }
}