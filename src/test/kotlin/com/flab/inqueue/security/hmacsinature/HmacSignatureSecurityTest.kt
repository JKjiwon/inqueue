package com.flab.inqueue.security.hmacsinature

import com.flab.inqueue.AcceptanceTest
import com.flab.inqueue.domain.member.entity.Member
import com.flab.inqueue.domain.member.entity.MemberKey
import com.flab.inqueue.domain.member.repository.MemberRepository
import com.flab.inqueue.domain.member.utils.memberkeygenrator.MemberKeyGenerator
import com.flab.inqueue.security.common.Role
import com.flab.inqueue.security.hmacsinature.utils.EncryptionUtil
import jakarta.transaction.Transactional
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class HmacSignatureSecurityTest : AcceptanceTest() {

    @Autowired
    lateinit var memberRepository: MemberRepository
    @Autowired
    lateinit var encryptionUtil: EncryptionUtil
    @Autowired
    lateinit var memberKeyGenerator: MemberKeyGenerator

    // ROLE_USER
    lateinit var testUser: Member
    lateinit var notEncryptedUserMemberKey: MemberKey
    lateinit var hmacSignaturePayloadWithUser: String

    // ROLE_ADMIN
    lateinit var testAdmin: Member
    lateinit var notEncryptedAdminMemberKey: MemberKey
    lateinit var hmacSignaturePayloadWithAdmin: String

    companion object {
        const val HMAC_SECURITY_TEST_URI = "/server/hmac-security-test"
        const val HMAC_SECURITY_TEST_WITH_ADMIN_USER_URI = "/server/hmac-security-test-with-admin-role"
    }

    @BeforeEach
    @Transactional
    fun setUp(@LocalServerPort port: Int) {
        // ROLE_USER
        hmacSignaturePayloadWithUser = "http://localhost:${port}" + HMAC_SECURITY_TEST_URI
        notEncryptedUserMemberKey = memberKeyGenerator.generate()
        testUser = Member(
            name = "USER",
            key = notEncryptedUserMemberKey.encrypt(encryptionUtil)
        )
        memberRepository.save(testUser)

        // ROLE_ADMIN
        hmacSignaturePayloadWithAdmin = "http://localhost:${port}" + HMAC_SECURITY_TEST_WITH_ADMIN_USER_URI
        notEncryptedAdminMemberKey = memberKeyGenerator.generate()
        testAdmin = Member(
            name = "ADMIN",
            key = notEncryptedAdminMemberKey.encrypt(encryptionUtil),
            roles = listOf(Role.USER, Role.ADMIN)
        )
        memberRepository.save(testAdmin)
    }

    @Test
    @DisplayName("Hmac Authentication 성공")
    fun hmac_authentication_success() {
        given.log().all()
            .header(
                HttpHeaders.AUTHORIZATION,
                createHmacAuthorizationHeader(
                    notEncryptedUserMemberKey.clientId,
                    createHmacSignature(hmacSignaturePayloadWithUser, notEncryptedUserMemberKey.clientSecret)
                )
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .get(HMAC_SECURITY_TEST_URI).
        then().log().all()
            .statusCode(HttpStatus.OK.value())
            .assertThat()
            .body("clientId", Matchers.equalTo(notEncryptedUserMemberKey.clientId))
            .body("userId", Matchers.nullValue())
            .body("roles", Matchers.hasItem("USER"))
    }

    @Test
    @DisplayName("clientSecret이 다른 경우, Hmac Authentication 실패")
    fun hmac_authentication_fail1() {
        val anotherMemberKey = memberKeyGenerator.generate()

        given.log().all()
            .header(
                HttpHeaders.AUTHORIZATION,
                createHmacAuthorizationHeader(
                    notEncryptedUserMemberKey.clientId,
                    createHmacSignature(hmacSignaturePayloadWithUser, anotherMemberKey.clientSecret)
                )
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .post(HMAC_SECURITY_TEST_URI).
        then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .assertThat()
            .body("message", Matchers.equalTo("Unauthorized"))
            .body("code",Matchers.equalTo(401))
    }

    @Test
    @DisplayName("clientId를 찾을 수 없는 경우, Hmac Authentication 실패")
    fun hmac_authentication_fail2() {
        val anotherMemberKey = memberKeyGenerator.generate()

        given.log().all()
            .header(
                HttpHeaders.AUTHORIZATION,
                createHmacAuthorizationHeader(
                    anotherMemberKey.clientId,
                    createHmacSignature(hmacSignaturePayloadWithUser, anotherMemberKey.clientSecret)
                )
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .post(HMAC_SECURITY_TEST_URI).
        then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .assertThat()
            .body("message", Matchers.equalTo("Unauthorized"))
            .body("code",Matchers.equalTo(401))
    }

    @Test
    @DisplayName("AUTHORIZATION 헤더가 없는 경우, Hmac Authentication 실패")
    fun hmac_authentication_fail3() {
        given.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .post(HMAC_SECURITY_TEST_URI).
        then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .assertThat()
            .body("message", Matchers.equalTo("Unauthorized"))
            .body("code",Matchers.equalTo(401))
    }

    @Test
    @DisplayName("ADMIN권한을 요구하는 API에 ADMIN이 인증 요청했을때, Hmac Authorization 성공")
    fun hmac_authentication_with_admin_role_success() {
        given.log().all()
            .header(
                HttpHeaders.AUTHORIZATION,
                createHmacAuthorizationHeader(
                    notEncryptedAdminMemberKey.clientId,
                    createHmacSignature(hmacSignaturePayloadWithAdmin, notEncryptedAdminMemberKey.clientSecret)
                )
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .get(HMAC_SECURITY_TEST_WITH_ADMIN_USER_URI).
        then().log().all()
            .statusCode(HttpStatus.OK.value())
            .assertThat()
            .body("clientId", Matchers.equalTo(notEncryptedAdminMemberKey.clientId))
            .body("userId", Matchers.nullValue())
            .body("roles", Matchers.hasItems("USER", "ADMIN"))
    }

    @Test
    @DisplayName("ADMIN권한을 요구하는 API에 USER가 인증 요청했을때, Hmac Authorization 실패")
    fun hmac_authentication_with_admin_role_fail() {
        given.log().all()
            .header(
                HttpHeaders.AUTHORIZATION,
                createHmacAuthorizationHeader(
                    notEncryptedUserMemberKey.clientId,
                    createHmacSignature(hmacSignaturePayloadWithAdmin, notEncryptedUserMemberKey.clientSecret)
                )
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE).
        `when`()
            .get(HMAC_SECURITY_TEST_WITH_ADMIN_USER_URI).
        then().log().all()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .assertThat()
            .body("message", Matchers.equalTo("Forbidden"))
            .body("code",Matchers.equalTo(403))
    }
}