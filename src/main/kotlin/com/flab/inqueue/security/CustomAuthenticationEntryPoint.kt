package com.flab.inqueue.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.flab.inqueue.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val unauthorized = ErrorCode.UNAUTHORIZED
        response.status = unauthorized.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val jsonResponse = com.flab.inqueue.exception.ErrorResponse(
            code = unauthorized.code,
            message = unauthorized.message,
        )

        response.writer.write(objectMapper.writeValueAsString(jsonResponse))
    }
}