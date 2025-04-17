package com.flab.inqueue.domain.queue.service

import com.flab.inqueue.AcceptanceTest
import com.flab.inqueue.domain.event.entity.Event
import com.flab.inqueue.domain.event.repository.EventRepository
import com.flab.inqueue.domain.member.entity.Member
import com.flab.inqueue.domain.member.entity.MemberKey
import com.flab.inqueue.domain.member.repository.MemberRepository
import com.flab.inqueue.domain.queue.entity.Job
import com.flab.inqueue.domain.queue.entity.JobStatus
import com.flab.inqueue.domain.queue.repository.JobRedisRepository
import com.flab.inqueue.domain.queue.repository.WaitQueueRedisRepository
import com.flab.inqueue.fixture.createEventRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate

class JobQueueEnterAllTest : AcceptanceTest() {
    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var jobService: JobService

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var jobRedisTemplate: RedisTemplate<String, Job>

    @Autowired
    lateinit var jobRedisRepository: JobRedisRepository

    @Autowired
    lateinit var waitQueueRedisRepository: WaitQueueRedisRepository

    lateinit var userId: String
    lateinit var clientId: String
    lateinit var member: Member

    lateinit var eventId1: String
    lateinit var event1: Event
    lateinit var eventId2: String
    lateinit var event2: Event

    @BeforeEach
    fun setUp() {
        userId = "userId"
        clientId = "clientId"
        member = Member(name = "member", key = MemberKey(clientId, "clientSecret"))
        memberRepository.save(member);

        eventId1 = "eventId1"
        event1 = createEventRequest(jobQueueSize = 10).toEntity(eventId1, member)
        eventId2 = "eventId2"
        event2 = createEventRequest(jobQueueSize = 10).toEntity(eventId2, member)
        eventRepository.saveAll(listOf(event1, event2))
    }

    @AfterEach
    fun tearDown() {
        eventRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
        jobRedisTemplate.execute { connection ->
            connection.serverCommands().flushDb()
            null
        }
    }

    @DisplayName("여러개의 Event의 대기열의 작업을 이동시킨다.")
    @Test
    fun enqueueJobs() {
        // given
        val eventId1JobQueueSize = 5L
        (0 until eventId1JobQueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId1, "user${(i + 1) * 10}", JobStatus.ENTER))
        }
        val eventId1WaitQueueSize = 2L
        (0 until eventId1WaitQueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId1, "user$i"))
        }

        val eventId2JobQueueSize = 5L
        (0 until eventId2JobQueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId2, "user${(i + 1) * 10}", JobStatus.ENTER))
        }
        val eventId2WaitQueueSize = 5L
        (0 until eventId2WaitQueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId2, "user$i"))
        }

        // when
        jobService.enqueue(listOf(event1, event2))

        // then
        assertThat(jobRedisRepository.size(eventId1)).isEqualTo(7)
        assertThat(waitQueueRedisRepository.size(eventId1)).isZero()

        assertThat(jobRedisRepository.size(eventId2)).isEqualTo(10)
        assertThat(waitQueueRedisRepository.size(eventId2)).isZero()
    }

    @DisplayName("대기열이 비어있으면 작업을 이동시키지 않는다.")
    @Test
    fun enqueueJobsWithEmptyWaitQueue() {
        // given
        val eventId1JobQueueSize = 5L
        (0 until eventId1JobQueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId1, "user${(i + 1) * 10}", JobStatus.ENTER))
        }
        // when
        jobService.enqueue(listOf(event1))

        // then
        assertThat(jobRedisRepository.size(eventId1)).isEqualTo(5)
        assertThat(waitQueueRedisRepository.size(eventId1)).isZero()
    }

    @DisplayName("작업열이 가득차면, 작업을 이동시키지 않은다.")
    @Test
    fun enqueueJobsWithFullJobQueue() {
        // given
        val eventId1JobQueueSize = 10L
        (0 until eventId1JobQueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId1, "user${(i + 1) * 10}", JobStatus.ENTER))
        }
        val eventId1WaitQueueSize = 2L
        (0 until eventId1WaitQueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId1, "user$i"))
        }

        val eventId2JobQueueSize = 5L
        (0 until eventId2JobQueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId2, "user${(i + 1) * 10}", JobStatus.ENTER))
        }
        val eventId2WaitQueueSize = 5L
        (0 until eventId2WaitQueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId2, "user$i"))
        }

        // when
        jobService.enqueue(listOf(event1, event2))

        // then
        assertThat(jobRedisRepository.size(eventId1)).isEqualTo(10)
        assertThat(waitQueueRedisRepository.size(eventId1)).isEqualTo(2)

        assertThat(jobRedisRepository.size(eventId2)).isEqualTo(10)
        assertThat(waitQueueRedisRepository.size(eventId2)).isZero()
    }

    @DisplayName("작업열의 용량을 고려하여 이동 가능한 수만큼 대기열에서 작업열로 이동시킨다.")
    @Test
    fun enqueueJobsWithinCapacity() {
        // given
        val eventId1JobQueueSize = 9L
        (0 until eventId1JobQueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId1, "user${(i + 1) * 10}", JobStatus.ENTER))
        }
        val eventId1WaitQueueSize = 2L
        (0 until eventId1WaitQueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId1, "user$i"))
        }

        val eventId2JobQueueSize = 7L
        (0 until eventId2JobQueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId2, "user${(i + 1) * 10}", JobStatus.ENTER))
        }
        val eventId2WaitQueueSize = 5L
        (0 until eventId2WaitQueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId2, "user$i"))
        }

        // when
        jobService.enqueue(listOf(event1, event2))

        // then
        assertThat(jobRedisRepository.size(eventId1)).isEqualTo(10)
        assertThat(waitQueueRedisRepository.size(eventId1)).isEqualTo(1)

        assertThat(jobRedisRepository.size(eventId2)).isEqualTo(10)
        assertThat(waitQueueRedisRepository.size(eventId2)).isEqualTo(2)
    }
}