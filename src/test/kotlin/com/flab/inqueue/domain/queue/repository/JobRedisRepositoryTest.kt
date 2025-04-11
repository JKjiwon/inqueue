package com.flab.inqueue.domain.queue.repository

import com.flab.inqueue.TestContainer
import com.flab.inqueue.domain.queue.entity.Job
import com.flab.inqueue.domain.queue.entity.JobStatus
import com.flab.inqueue.support.RedisTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.redis.core.RedisTemplate
import java.util.*

@RedisTest
@ComponentScan(basePackages = ["com.flab.inqueue.domain.queue.repository"])
class JobRedisRepositoryTest : TestContainer() {

    @Autowired
    private lateinit var jobRedisRepository: JobRedisRepository

    @Autowired
    private lateinit var userRedisTemplate: RedisTemplate<String, String>

    @DisplayName("Job 사이즈를 측정한다.")
    @Test
    fun size() {
        // given
        val eventId = UUID.randomUUID().toString()
        val size = 10L
        (0 until size).forEach { i ->
            jobRedisRepository.save(Job(eventId, "user$i", JobStatus.ENTER))
        }
        // when
        val result = jobRedisRepository.size(eventId)

        // then
        assertThat(result).isEqualTo(size)
    }

    @DisplayName("이벤트Id 리스트를 받아서 큐의 사이즈들을 반환한다.")
    @Test
    fun sizes() {
        // given
        val eventId1 = UUID.randomUUID().toString()
        val eventId1QueueSize = 10L
        (0 until eventId1QueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId1, "user$i", JobStatus.ENTER))
        }

        val eventId2 = UUID.randomUUID().toString()
        val eventId2QueueSize = 20L
        (0 until eventId2QueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId2, "user$i", JobStatus.ENTER))
        }

        val eventId3 = UUID.randomUUID().toString()
        val eventId3QueueSize = 30L
        (0 until eventId3QueueSize).forEach { i ->
            jobRedisRepository.save(Job(eventId3, "user$i", JobStatus.ENTER))
        }

        println(jobRedisRepository.size(eventId1))

        // when
        val sizes = jobRedisRepository.sizes(
            listOf(
                eventId1,
                eventId2,
                eventId3
            )
        )

        // then
        assertThat(sizes).hasSize(3)
            .extracting("eventId", "size")
            .containsExactly(
                tuple(eventId1, eventId1QueueSize),
                tuple(eventId2, eventId2QueueSize),
                tuple(eventId3, eventId3QueueSize),
            )
    }

    @DisplayName("Job 리스트를 저장한다.")
    @Test
    fun saveAll() {
        // given
        val eventId = UUID.randomUUID().toString()
        val size = 10L
        val jobs = mutableListOf<Job>()
        (0 until size).forEach { i ->
            jobs.add(Job(eventId, "user$i", JobStatus.ENTER, 10L))
        }
        // when
        jobRedisRepository.saveAll(jobs)

        // then
        assertThat(jobRedisRepository.size(eventId)).isEqualTo(size)
        jobs.forEach {
                job -> assertThat(userRedisTemplate.opsForValue().get(job.redisValue)).isNotNull()
        }
    }
}
