package com.flab.inqueue.security

import com.fasterxml.jackson.databind.ObjectMapper
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

    companion object {
        private const val DEFAULT_UNAUTHORIZED_ERROR_MESSAGE = "Unauthorized"
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE


        val jsonResponse = com.flab.inqueue.exception.ErrorResponse(
                code = HttpServletResponse.SC_UNAUTHORIZED,
                message = DEFAULT_UNAUTHORIZED_ERROR_MESSAGE,
        )

        response.writer.write(objectMapper.writeValueAsString(jsonResponse))
    }
}