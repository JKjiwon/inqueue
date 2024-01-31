package com.flab.inqueue.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.flab.inqueue.exception.ErrorCode
import com.flab.inqueue.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDenierHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val forbidden = ErrorCode.FORBIDDEN
        response.status = forbidden.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val jsonResponse = ErrorResponse(
            code = forbidden.code,
            message = forbidden.message
        )

        response.writer.write(objectMapper.writeValueAsString(jsonResponse))
    }
}