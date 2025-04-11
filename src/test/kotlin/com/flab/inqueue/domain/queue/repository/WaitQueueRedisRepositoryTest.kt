package com.flab.inqueue.domain.queue.repository

import com.flab.inqueue.TestContainer
import com.flab.inqueue.domain.queue.dto.QueueInfo
import com.flab.inqueue.domain.queue.entity.Job
import com.flab.inqueue.support.RedisTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.redis.core.RedisTemplate
import java.util.*

@ComponentScan(basePackages = ["com.flab.inqueue.domain.queue.repository"])
@RedisTest
class WaitQueueRedisRepositoryTest : TestContainer() {

    @Autowired
    private lateinit var waitQueueRedisRepository: WaitQueueRedisRepository

    @Autowired
    private lateinit var userRedisTemplate: RedisTemplate<String, String>

    @DisplayName("Job 사이즈를 측정한다.")
    @Test
    fun size() {
        // given
        val eventId = UUID.randomUUID().toString()
        val size = 10L
        (0 until size).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId, "user$i"))
        }
        // when
        val result = waitQueueRedisRepository.size(eventId)

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
            waitQueueRedisRepository.save(Job(eventId1, "user$i"))
        }

        val eventId2 = UUID.randomUUID().toString()
        val eventId2QueueSize = 20L
        (0 until eventId2QueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId2, "user$i"))
        }

        val eventId3 = UUID.randomUUID().toString()
        val eventId3QueueSize = 30L
        (0 until eventId3QueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId3, "user$i"))
        }

        // when
        val sizes = waitQueueRedisRepository.sizes(
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

    @DisplayName("큐의 사이즈가 0인 경우 <eventId, 0>의 형태로 반환한다.")
    @Test
    fun sizesWithOneEmptyQueue() {
        // given
        val eventId1 = UUID.randomUUID().toString()

        // when
        val sizes = waitQueueRedisRepository.sizes(
            listOf(
                eventId1
            )
        )
        // then
        assertThat(sizes).hasSize(1)
            .extracting("eventId", "size")
            .containsExactly(
                tuple(eventId1, 0L)
            )
    }

    @DisplayName("큐들 중 하나의 사이즈가 0인 경우 사이즈가 0인 큐는 <eventId, 0>의 형태로 반환한다.")
    @Test
    fun sizesWithEmptyQueueIncluded() {
        // given
        val eventId1 = UUID.randomUUID().toString()
        val eventId1QueueSize = 10L
        (0 until eventId1QueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId1, "user$i"))
        }

        val eventId2 = UUID.randomUUID().toString()

        val eventId3 = UUID.randomUUID().toString()
        val eventId3QueueSize = 30L
        (0 until eventId3QueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId3, "user$i"))
        }

        // when
        val sizes = waitQueueRedisRepository.sizes(
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
                tuple(eventId2, 0L),
                tuple(eventId3, eventId3QueueSize),
            )
    }

    @DisplayName("SortedSet의 상위 n개의 Job을 조회한다.")
    @Test
    fun popMin() {
        // given
        val eventId = UUID.randomUUID().toString()
        val size = 10L
        (0 until size).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId, "user$i"))
        }

        // when
        val jobs = waitQueueRedisRepository.popMin(eventId, size)

        // then
        assertThat(jobs.size).isEqualTo(size)
        assertThat(waitQueueRedisRepository.size(eventId)).isZero()
        jobs.forEach { job ->
            assertThat(userRedisTemplate.opsForValue().get(job.redisValue)).isNull()
        }
    }

    @DisplayName("여러개의 SortedSet의 상위 n개의 Job을 조회한다.")
    @Test
    fun popMin_multiple() {
        // given
        val eventId1 = UUID.randomUUID().toString()
        val eventId1QueueSize = 2L
        (0 until eventId1QueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId1, "user$i"))
        }

        val eventId2 = UUID.randomUUID().toString()
        val eventId2QueueSize = 4L
        (0 until eventId2QueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId2, "user$i"))
        }

        val eventId3 = UUID.randomUUID().toString()
        val eventId3QueueSize = 6L
        (0 until eventId3QueueSize).forEach { i ->
            waitQueueRedisRepository.save(Job(eventId3, "user$i"))
        }

        // when
        val jobs = waitQueueRedisRepository.popMin(
            listOf(
                QueueInfo(eventId1, 1),
                QueueInfo(eventId2, 2),
                QueueInfo(eventId3, 3)

            )
        )

        // then
        assertThat(jobs.size).isEqualTo(6)
        assertThat(waitQueueRedisRepository.size(eventId1)).isEqualTo(1)
        assertThat(waitQueueRedisRepository.size(eventId2)).isEqualTo(2)
        assertThat(waitQueueRedisRepository.size(eventId3)).isEqualTo(3)

        jobs.forEach { job ->
            assertThat(userRedisTemplate.opsForValue().get(job.redisValue)).isNull()
        }
    }
}
