package com.flab.inqueue.domain.queue.service

import com.flab.inqueue.domain.event.entity.Event
import com.flab.inqueue.domain.event.repository.EventRepository
import com.flab.inqueue.domain.member.entity.Member
import com.flab.inqueue.domain.member.entity.MemberKey
import com.flab.inqueue.domain.queue.repository.JobRedisRepository
import com.flab.inqueue.fixture.createEventRequest
import com.flab.inqueue.support.UnitTest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class JobQueueEnterAllTest {

    private val jobRedisRepository: JobRedisRepository = mockk<JobRedisRepository>(relaxed = true)
    private val eventRepository: EventRepository = mockk<EventRepository>()
    private val waitQueueService: WaitQueueService = mockk<WaitQueueService>(relaxed = true)
    private val jobService: JobService = JobService(jobRedisRepository, eventRepository, waitQueueService)

    lateinit var userId: String
    lateinit var eventId: String
    lateinit var clientId: String
    lateinit var member: Member
    lateinit var event: Event

    @BeforeEach
    fun setUp() {
        userId = "testUserId"
        eventId = "testEventId"
        clientId = "testClientId"
        member = Member(name = "testMember", key = MemberKey(clientId, "testClientSecret"))
        event = createEventRequest().toEntity(eventId, member)
        every { eventRepository.findByEventId(eventId) } returns event
    }

//    @DisplayName("")
//    @Test
//    fun enterAll() {
//        TODO()
//    }

}