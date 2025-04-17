package com.flab.inqueue.domain.queue.repository

import com.flab.inqueue.domain.queue.dto.QueueInfo
import com.flab.inqueue.domain.queue.dto.QueueSize
import com.flab.inqueue.domain.queue.entity.Job
import com.flab.inqueue.domain.queue.entity.JobStatus
import com.flab.inqueue.domain.queue.exception.RedisDataAccessException
import org.springframework.data.redis.connection.StringRedisConnection
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class WaitQueueRedisRepository(
    private val jobRedisTemplate: RedisTemplate<String, Job>,
    private val userRedisTemplate: RedisTemplate<String, String>,
) {
    fun save(job: Job): Long {
        jobRedisTemplate.opsForZSet().add(job.redisKey, job, System.nanoTime().toDouble())
        userRedisTemplate.opsForValue().set(job.redisValue, job.redisValue, job.queueLimitTime, TimeUnit.SECONDS)
        return rank(job)
    }

    fun rank(job: Job): Long {
        return jobRedisTemplate.opsForZSet().rank(job.redisKey, job)
            ?: throw RedisDataAccessException("데이터에 접근 할 수 없습니다.")
    }

    fun remove(job: Job) {
        jobRedisTemplate.opsForZSet().remove(job.redisKey, job)
            ?: throw RedisDataAccessException("데이터에 접근 할 수 없습니다.")
        userRedisTemplate.opsForValue().getAndDelete(job.redisValue)
            ?: throw RedisDataAccessException("데이터에 접근 할 수 없습니다.")
    }

    fun isMember(job: Job): Boolean {
        val hasUser = userRedisTemplate.opsForValue().get(job.redisValue)
        return hasUser != null
    }

    fun updateUserTtl(job: Job) {
        userRedisTemplate.opsForValue().set(job.redisValue, job.redisValue, job.queueLimitTime, TimeUnit.SECONDS)
    }

    fun popMin(eventId: String, size: Long): List<Job> {
        val redisKey = JobStatus.WAIT.makeRedisKey(eventId)
        val jobs = jobRedisTemplate.opsForZSet().popMin(redisKey, size)?.mapNotNull { it.value }
            ?: throw RedisDataAccessException("데이터에 접근 할 수 없습니다.")

        userRedisTemplate.executePipelined { connection ->
            val redisConnection = connection as StringRedisConnection
            for (job in jobs) {
                redisConnection.del(job.redisValue)
            }
            null
        }
        return jobs
    }

    fun popMin(queueInfos: List<QueueInfo>): List<Job> {
        val results = jobRedisTemplate.executePipelined { connection ->
            queueInfos.forEach { queueInfo ->
                val redisKey = JobStatus.WAIT.makeRedisKey(queueInfo.eventId)
                connection.zSetCommands().zPopMin(redisKey.toByteArray(), queueInfo.size)
            }
            null
        }

        val jobs = results.filterIsInstance<Set<*>>()
            .flatten()
            .mapNotNull { it as? ZSetOperations.TypedTuple<*> }
            .mapNotNull { it.value as? Job }

        userRedisTemplate.executePipelined { connection ->
            val redisConnection = connection as StringRedisConnection
            for (job in jobs) {
                redisConnection.del(job.redisValue)
            }
            null
        }
        return jobs
    }

    fun size(eventId: String): Long {
        val redisKey = JobStatus.WAIT.makeRedisKey(eventId)
        return jobRedisTemplate.opsForZSet().size(redisKey) ?: throw RedisDataAccessException("데이터에 접근 할 수 없습니다.")
    }

    fun sizes(eventId: List<String>): List<QueueSize> {
        val redisKeys = eventId.map { JobStatus.WAIT.makeRedisKey(it) }
        val sizes = jobRedisTemplate.executePipelined { connection ->
            redisKeys.forEach { redisKey ->
                connection.zSetCommands().zCard(redisKey.toByteArray())
            }
            null
        }

        return eventId.zip(sizes.map { it as Long })
            .map { (key, size) -> QueueSize(key, size) }
    }

}