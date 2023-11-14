package com.flab.inqueue.config.redis

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperty(
    val host: String,
    val port: Int,
)