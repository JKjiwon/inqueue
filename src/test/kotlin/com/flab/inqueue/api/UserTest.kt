package com.flab.inqueue.api

import com.flab.inqueue.AcceptanceTest
import com.flab.inqueue.REST_DOCS_DOCUMENT_IDENTIFIER
import com.flab.inqueue.dto.AuthRequest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.*
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.restdocs.restassured.RestAssuredRestDocumentation
import org.springframework.restdocs.restassured.RestDocumentationFilter
import org.springframework.restdocs.snippet.Snippet

class UserTest : AcceptanceTest() {

    @Test
    @DisplayName("토큰 발급 api")
    fun generateToken() {
        val authRequest = AuthRequest("testEvent1")

        given(this.spec).log().all()
            .filter(GenerateTokenDocument.FILTER)
            .header(HttpHeaders.AUTHORIZATION, "ClientId:(StringToSign를 ClientSecret으로 Hmac 암호화)")
            .body(authRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .post("v1/auth/token").
        then().log().all()
            .statusCode(HttpStatus.OK.value())
            .body("accessToken", notNullValue())
    }

    @Test
    @DisplayName("사용자 대기열 진입 api")
    fun enterWaitQueue() {
        val eventId = "testEvent1"

        given(this.spec).log().all()
            .filter(EnterWaitQueueDocument.FILTER)
            .header(HttpHeaders.AUTHORIZATION, "AccessToken")
            .header("clientId", "String")
            .pathParam("eventId", eventId)
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .post("v1/events/{eventId}/enter").
        then().log().all()
            .statusCode(HttpStatus.OK.value())
            .assertThat()
            .body("status", notNullValue())
            .body("expectedInfo.time", notNullValue())
            .body("expectedInfo.order", notNullValue())
    }

    @Test
    @DisplayName("사용자 대기열 조회 api")
    fun retrieveWaitQueue() {
        val eventId = "testEvent1"

        given(this.spec).log().all()
            .filter(RetrieveWaitQueueDocument.FILTER)
            .header(HttpHeaders.AUTHORIZATION, "AccessToken")
            .header("clientId", "String")
            .pathParam("eventId", eventId)
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .get("v1/events/{eventId}").
        then().log().all()
            .statusCode(HttpStatus.OK.value())
            .assertThat()
            .body("status", notNullValue())
            .body("expectedInfo.time", notNullValue())
            .body("expectedInfo.order", notNullValue())
    }
}

object GenerateTokenDocument {
    val FILTER: RestDocumentationFilter = RestAssuredRestDocumentation.document(
        REST_DOCS_DOCUMENT_IDENTIFIER,
        headerFiledSnippet(),
        requestFieldsSnippet(),
        responseFieldsSnippet()
    )

    private fun headerFiledSnippet(): Snippet {
        return requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description("ClientId:(StringToSign를 ClientSecret으로 Hmac 암호화)"),
            headerWithName("clientId").description("고객사 식별자"),
            headerWithName(HttpHeaders.CONTENT_TYPE).description("요청-Type"),
        )
    }

    private fun requestFieldsSnippet(): Snippet {
        return requestFields(
            fieldWithPath("eventId").type(JsonFieldType.STRING).description("이벤트 식별자"),
            fieldWithPath("userId").type(JsonFieldType.STRING).description("사용자 식별자"),
        )
    }

    private fun responseFieldsSnippet(): Snippet {
        return responseFields(
            fieldWithPath(HttpHeaders.AUTHORIZATION).type(JsonFieldType.STRING).description("JWT 토큰"),
        )
    }
}

object EnterWaitQueueDocument {
    val FILTER: RestDocumentationFilter = RestAssuredRestDocumentation.document(
        REST_DOCS_DOCUMENT_IDENTIFIER,
        headerFiledSnippet(),
        pathParametersSnippet(),
        responseFieldsSnippet()
    )

    private fun headerFiledSnippet(): Snippet {
        return requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description("ClientId:(StringToSign를 ClientSecret으로 Hmac 암호화)"),
            headerWithName("clientId").description("고객사 식별자"),
            headerWithName(HttpHeaders.CONTENT_TYPE).description("요청-Type"),
        )
    }

    private fun pathParametersSnippet(): Snippet {
        return pathParameters(
            parameterWithName("eventId").description("이벤트 식별자"),
        )
    }

    private fun responseFieldsSnippet(): Snippet {
        return responseFields(
            fieldWithPath("status").type(JsonFieldType.STRING).description("대기 상태(ENTER, WAIT)"),
            fieldWithPath("expectedInfo.time").type(JsonFieldType.STRING).description("대기 시간"),
            fieldWithPath("expectedInfo.order").type(JsonFieldType.NUMBER).description("대기 순번"),
        )
    }
}

object RetrieveWaitQueueDocument {
    val FILTER: RestDocumentationFilter = RestAssuredRestDocumentation.document(
        REST_DOCS_DOCUMENT_IDENTIFIER,
        headerFiledSnippet(),
        pathParametersSnippet(),
        responseFieldsSnippet()
    )

    private fun headerFiledSnippet(): Snippet {
        return requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION)
                .description("ClientId:(StringToSign를 ClientSecret으로 Hmac 암호화)"),
            headerWithName("clientId").description("고객사 식별자"),
            headerWithName(HttpHeaders.CONTENT_TYPE).description("요청-Type"),
        )
    }

    private fun pathParametersSnippet(): Snippet {
        return pathParameters(
            parameterWithName("eventId").description("이벤트 식별자"),
        )
    }

    private fun responseFieldsSnippet(): Snippet {
        return responseFields(
            fieldWithPath("status").type(JsonFieldType.STRING).description("대기 상태(ENTER, WAIT)"),
            fieldWithPath("expectedInfo.time").type(JsonFieldType.STRING).description("대기 시간"),
            fieldWithPath("expectedInfo.order").type(JsonFieldType.NUMBER).description("대기 순번"),
        )
    }
}
