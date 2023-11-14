package com.flab.inqueue.domain.event.entity

import com.flab.inqueue.domain.BaseEntity
import com.flab.inqueue.domain.member.entity.Member
import jakarta.persistence.*

@Table(
    uniqueConstraints = [UniqueConstraint(name = "uk_event", columnNames = ["eventId"])],
    indexes = [Index(name = "idx_event_id", columnList = "eventId")]
)
@Entity
class Event(
    @Column(nullable = false) val eventId: String,
    @Embedded var period: WaitQueuePeriod,
    @Column(nullable = false) var jobQueueSize: Long,
    @Column(nullable = false) var jobQueueLimitTime: Long,
    var redirectUrl: String?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,
) : BaseEntity() {
    fun update(event: Event) {
        this.period = event.period
        this.jobQueueSize = event.jobQueueSize
        this.jobQueueLimitTime = event.jobQueueLimitTime
        this.redirectUrl = event.redirectUrl
    }

    fun isAccessible(clientId : String) :Boolean {
        return this.member.key.clientId == clientId
    }
}
