package com.flab.inqueue.config.redis

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.flab.inqueue.domain.queue.entity.Job
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
class RedisConfig(
    private val redisProperty: RedisProperty,
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration()
        redisStandaloneConfiguration.hostName = redisProperty.host
        redisStandaloneConfiguration.port = redisProperty.port
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    @Bean
    fun jobRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Job> {
        val redisTemplate = RedisTemplate<String, Job>()
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        redisTemplate.setEnableTransactionSupport(true)
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = Jackson2JsonRedisSerializer(objectMapper, Job::class.java)
        redisTemplate.setConnectionFactory(connectionFactory)
        return redisTemplate
    }

    @Bean
    fun userRedisTemplate(): RedisTemplate<String, String> {
        val redisTemplate = StringRedisTemplate()
        redisTemplate.setEnableTransactionSupport(true)
        redisTemplate.setConnectionFactory(redisConnectionFactory())
        return redisTemplate
    }
}