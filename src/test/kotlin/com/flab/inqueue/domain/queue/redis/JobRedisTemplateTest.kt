package com.flab.inqueue.domain.queue.redis

import com.flab.inqueue.TestContainer
import com.flab.inqueue.domain.queue.entity.Job
import com.flab.inqueue.support.RedisTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import java.util.*


@RedisTest
class JobRedisTemplateTest @Autowired constructor(
    private val jobRedisTemplate: RedisTemplate<String, Job>,
) : TestContainer() {
    private val logger = LoggerFactory.getLogger(JobRedisTemplateTest::class.java)
    private val zSetOperations = jobRedisTemplate.opsForZSet();

    @Test
    @DisplayName("레디스 실행 테스트")
    fun redisRunningTest() {
        assertTrue(redisContainer.isRunning);
    }

    @Test
    @DisplayName("등록")
    fun add() {
        //given
        val eventId = UUID.randomUUID().toString()
        val job = Job(eventId, UUID.randomUUID().toString())
        zSetOperations.add(eventId, job, System.nanoTime().toDouble())
        //when
        val range = zSetOperations.range(eventId, 0, -1)
        //then
        assertThat(range?.size).isEqualTo(1)
    }

    @Test
    @DisplayName("key의 전체 사이즈(갯수) 조회")
    fun getKeySize() {
        //given
        val eventId = UUID.randomUUID().toString()
        zSetOperations.add(
            eventId, Job(eventId, UUID.randomUUID().toString()), System.nanoTime().toDouble()
        )
        zSetOperations.add(
            eventId, Job(eventId, UUID.randomUUID().toString()), System.nanoTime().toDouble()
        )
        zSetOperations.add(
            eventId, Job(eventId, UUID.randomUUID().toString()), System.nanoTime().toDouble()
        )
        //when
        val long = zSetOperations.size(eventId)
        logger.info("{}", long)
        //then
        assertThat(long).isEqualTo(3)
    }

    @Test
    @DisplayName("key의 rank 조회")
    fun getRanksOfKey() {
        //given
        val eventId = UUID.randomUUID().toString()
        val job1 = Job(eventId, UUID.randomUUID().toString())
        val job2 = Job(eventId, UUID.randomUUID().toString())
        val job3 = Job(eventId, UUID.randomUUID().toString())
        zSetOperations.add(eventId, job1, System.nanoTime().toDouble())
        zSetOperations.add(eventId, job2, System.nanoTime().toDouble())
        zSetOperations.add(eventId, job3, System.nanoTime().toDouble())


        //when
        val totalSize = zSetOperations.size(eventId)
        val rank1 = zSetOperations.rank(eventId, job1)

        //then
        assertThat(rank1).isEqualTo(0)
        assertThat(totalSize).isEqualTo(3)
    }


    @Test
    @DisplayName("key 삭제")
    fun deleteKey() {
        //given
        val eventId = UUID.randomUUID().toString()
        val job1 = Job(eventId, UUID.randomUUID().toString())
        val job2 = Job(eventId, UUID.randomUUID().toString())
        val job3 = Job(eventId, UUID.randomUUID().toString())
        zSetOperations.add(eventId, job1, System.nanoTime().toDouble())
        zSetOperations.add(eventId, job2, System.nanoTime().toDouble())
        zSetOperations.add(eventId, job3, System.nanoTime().toDouble())


        //when
        val remove = zSetOperations.remove(eventId, job1)
        val totalSize = zSetOperations.size(eventId)

        //then
        assertThat(remove).isNotNull
        assertThat(totalSize).isEqualTo(2)
    }

    @Test
    @DisplayName("key의 범위 조회")
    fun getRetrieveFromRange() {
        //given
        val eventId = UUID.randomUUID().toString()
        repeat(100) {
            zSetOperations.add(
                eventId, Job(eventId, UUID.randomUUID().toString()), System.nanoTime().toDouble()
            )
        }

        //when
        val range = zSetOperations.range(eventId, 0, 9)

        //then
        assertThat(range?.size).isEqualTo(10)
    }

    @Test
    @DisplayName("key의 범위 삭제")
    fun deleteFromRangeWithKey() {
        //given
        val eventId = UUID.randomUUID().toString()
        repeat(100) {
            zSetOperations.add(
                eventId, Job(eventId, UUID.randomUUID().toString()), System.nanoTime().toDouble()
            )
        }

        //when
        zSetOperations.removeRange(eventId, 0, 9)
        val size = zSetOperations.size(eventId)
        //then
        assertThat(size).isEqualTo(90)
    }
}