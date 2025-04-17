package com.flab.inqueue.support

import com.flab.inqueue.config.redis.RedisConfig
import com.flab.inqueue.config.redis.RedisProperty
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
annotation class TestEnvironment

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(MockKExtension::class)
annotation class UnitTest

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestEnvironment
annotation class RepositoryTest

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@TestEnvironment
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(RestDocumentationExtension::class)
annotation class IntegrationTest


@TestEnvironment
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataRedisTest
@Import(RedisConfig::class)
@EnableConfigurationProperties(value = [RedisProperty::class])
annotation class RedisTest