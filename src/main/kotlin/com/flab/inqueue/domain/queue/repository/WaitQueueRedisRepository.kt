package com.flab.inqueue.domain.queue.repository

import com.flab.inqueue.domain.queue.entity.Job
import com.flab.inqueue.domain.queue.exception.RedisDataAccessException
import org.springframework.data.redis.connection.StringRedisConnection
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class WaitQueueRedisRepository(
    private val jobRedisTemplate: RedisTemplate<String, Job>,
    private val userRedisTemplate: RedisTemplate<String, String>,
) {
    fun register(job: Job): Long {
        jobRedisTemplate.opsForZSet().add(job.redisKey, job, System.nanoTime().toDouble())
        userRedisTemplate.opsForValue().set(job.redisValue, job.redisValue, job.queueLimitTime, TimeUnit.SECONDS)
        return rank(job)
    }

    fun size(key: String): Long {
        return jobRedisTemplate.opsForZSet().size(key) ?: throw RedisDataAccessException("데이터에 접근 할 수 없습니다.")
    }

    fun range(key: String, start: Long, end: Long): MutableSet<Job> {
        return jobRedisTemplate.opsForZSet().range(key, start, end)
            ?: throw RedisDataAccessException("데이터에 접근 할 수 없습니다.")
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

    fun findMe(job: Job): Cursor<ZSetOperations.TypedTuple<Job>> {
        return jobRedisTemplate.opsForZSet().scan(job.redisKey, ScanOptions.NONE)
    }

    fun isMember(job: Job): Boolean {
        val hasUser = userRedisTemplate.opsForValue().get(job.redisValue)
        return hasUser != null
    }

    fun updateUserTtl(job: Job) {
        userRedisTemplate.opsForValue().set(job.redisValue, job.redisValue, job.queueLimitTime, TimeUnit.SECONDS)
    }

    fun popMin(key: String, size: Long): MutableSet<ZSetOperations.TypedTuple<Job>> {
        val jobTuples = jobRedisTemplate.opsForZSet().popMin(key, size)
            ?: throw RedisDataAccessException("데이터에 접근 할 수 없습니다.")

        userRedisTemplate.executePipelined { connection ->
            val redisConnection = connection as StringRedisConnection
            for (jobTuple in jobTuples) {
                redisConnection.del(jobTuple.value!!.redisValue)
            }
            null
        }
        return jobTuples
    }
}