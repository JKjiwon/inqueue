package com.flab.inqueue.security

import com.fasterxml.jackson.databind.ObjectMapper
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

    companion object {
        private const val DEFAULT_FORBIDDEN_ERROR_MESSAGE = "Forbidden"
    }

    override fun handle(
            request: HttpServletRequest,
            response: HttpServletResponse,
            accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val jsonResponse = ErrorResponse(
                code = HttpServletResponse.SC_FORBIDDEN,
                message = DEFAULT_FORBIDDEN_ERROR_MESSAGE,
        )

        response.writer.write(objectMapper.writeValueAsString(jsonResponse))
    }
}