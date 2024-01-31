package com.flab.inqueue.domain.event.dto

import com.flab.inqueue.domain.event.entity.Event
import com.flab.inqueue.domain.event.entity.WaitQueuePeriod
import com.flab.inqueue.domain.member.entity.Member
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime


data class EventRequest(
    @field:NotNull var waitQueueStartTime: LocalDateTime? = null,
    @field:NotNull val waitQueueEndTime: LocalDateTime? = null,
    @field:NotNull val jobQueueSize: Long? = null,
    @field:NotNull val jobQueueLimitTime: Long? = null,

    val redirectUrl: String? = null,
) {
    fun toEntity(eventId: String, member: Member): Event {
        return Event(
            eventId,
            WaitQueuePeriod(waitQueueStartTime!!, waitQueueEndTime!!),
            jobQueueSize!!,
            jobQueueLimitTime!!,
            redirectUrl,
            member
        )
    }
}