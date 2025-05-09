package com.flab.inqueue.domain.event.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
data class WaitQueuePeriod(
    @Column(nullable = false) val startDateTime: LocalDateTime,
    @Column(nullable = false) val endDateTime: LocalDateTime,
) {
    init {
        require(endDateTime >= startDateTime) { "시작 일시는 종료 일시보다 이후일 수 없습니다." }
    }

    fun contains(value: LocalDateTime): Boolean = (startDateTime..endDateTime).contains(value)
}
