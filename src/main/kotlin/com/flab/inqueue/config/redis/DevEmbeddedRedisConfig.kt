package com.flab.inqueue.config.redis

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisServer

@Profile("dev")
@Configuration
class DevEmbeddedRedisConfig(
        redisProperty: RedisProperty
) {
    lateinit var redisServer: RedisServer

    init {
        redisServer = RedisServer(redisProperty.port)
    }

    @PostConstruct
    fun startRedis() {
        redisServer.start()
    }

    @PreDestroy
    fun stopRedis() {
        redisServer.stop()
    }
}