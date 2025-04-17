package com.flab.inqueue.domain.queue.dto

data class QueueSize(
    val eventId: String,
    val size: Long,
)