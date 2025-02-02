package com.flab.inqueue.security.jwt

import com.flab.inqueue.AcceptanceTest
import com.flab.inqueue.security.jwt.utils.JwtProperties
import com.flab.inqueue.security.jwt.utils.JwtToken
import com.flab.inqueue.security.jwt.utils.JwtUtils
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.security.SecureRandom
import java.util.*

class JwtSecurityTest : AcceptanceTest() {

    @Autowired
    lateinit var jwtUtils: JwtUtils

    @Autowired
    private lateinit var jwtProperties: JwtProperties

    companion object {
        private const val JWT_TOKEN_PREFIX = "Bearer "
        const val JWT_SECURITY_TEST_URI = "/client/jwt-security-test"
        private const val TEST_USER_ID = "testUserId"
        private const val TEST_CLIENT_ID = "testUserId"
    }

    @Test
    @DisplayName("JWT Authentication 성공")
    fun jwt_authentication_success() {
        given.log().all()
            .header(
                HttpHeaders.AUTHORIZATION,
                JWT_TOKEN_PREFIX + getJwtToken(TEST_USER_ID, TEST_CLIENT_ID).accessToken
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .get(JWT_SECURITY_TEST_URI).
        then().log().all()
            .statusCode(HttpStatus.OK.value())
            .assertThat()
            .body("clientId", Matchers.equalTo(TEST_CLIENT_ID))
            .body("userId", Matchers.equalTo(TEST_USER_ID))
            .body("roles", Matchers.hasItem("USER"))
    }

    @Test
    @DisplayName("헤더에 AUTHORIZATION 값이 없는 경우, JWT Authentication 실패")
    fun jwt_authentication_fail1() {
        given.log().all()
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .get(JWT_SECURITY_TEST_URI).
        then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .assertThat()
            .body("message", Matchers.equalTo("Unauthorized"))
            .body("code",Matchers.equalTo(401))
    }

    @Test
    @DisplayName("JWT 토큰의 서명키가 다른 경우, JWT Authentication 실패")
    fun jwt_authentication_fail2() {
        given.log().all()
            .header(
                HttpHeaders.AUTHORIZATION,
                JWT_TOKEN_PREFIX + getJwtTokenWithAnotherSecretKey(TEST_USER_ID, TEST_CLIENT_ID).accessToken
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .get(JWT_SECURITY_TEST_URI).
        then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .assertThat()
            .body("message", Matchers.equalTo("Unauthorized"))
            .body("code",Matchers.equalTo(401))
    }

    @Test
    @DisplayName("JWT 토큰이 만료 된 경우, JWT Authentication 실패")
    fun jwt_authentication_fail3() {
        given.log().all()
            .header(
                HttpHeaders.AUTHORIZATION,
                JWT_TOKEN_PREFIX + getExpiredJwtToken(TEST_USER_ID, TEST_CLIENT_ID).accessToken
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .get(JWT_SECURITY_TEST_URI).
        then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .assertThat()
            .body("message", Matchers.equalTo("Unauthorized"))
            .body("code",Matchers.equalTo(401))
    }

    private fun getJwtToken(clientId: String, userId: String): JwtToken {
        return jwtUtils.create(clientId, userId)
    }

    private fun getJwtTokenWithAnotherSecretKey(clientId: String, userId: String): JwtToken {
        val newProperties = JwtProperties(createSecretKey(), 3600)
        val jwtUtils = JwtUtils(newProperties)
        return jwtUtils.create(clientId, userId)
    }

    private fun getExpiredJwtToken(clientId: String, userId: String): JwtToken {
        val newProperties = JwtProperties(jwtProperties.secretKey, 10)
        val jwtUtils = JwtUtils(newProperties)
        val jwtToken = jwtUtils.create(clientId, userId)
        Thread.sleep(100)
        return jwtToken
    }

    private fun createSecretKey(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
}