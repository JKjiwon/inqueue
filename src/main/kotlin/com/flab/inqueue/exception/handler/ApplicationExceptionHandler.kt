package com.flab.inqueue.exception.handler

import com.flab.inqueue.exception.ApplicationException
import com.flab.inqueue.exception.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApplicationExceptionHandler {

    private val log = LoggerFactory.getLogger(ApplicationExceptionHandler::class.java)

    @ExceptionHandler(value = [ApplicationException::class])
    fun applicationException(exception: ApplicationException): ResponseEntity<ErrorResponse> {
        log.error("ApplicationException", exception)

        return ResponseEntity
            .status(exception.httpStatus.value())
            .body(
                ErrorResponse(exception.code, exception.message!!)
            )
    }
}