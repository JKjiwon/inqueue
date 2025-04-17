package com.flab.inqueue.domain.queue.service

import com.flab.inqueue.domain.queue.dto.JobResponse
import com.flab.inqueue.domain.queue.dto.QueueInfo
import com.flab.inqueue.domain.queue.dto.QueueSize
import com.flab.inqueue.domain.queue.dto.WaitQueueInfo
import com.flab.inqueue.domain.queue.entity.Job
import com.flab.inqueue.domain.queue.entity.JobStatus
import com.flab.inqueue.domain.queue.repository.WaitQueueRedisRepository
import org.springframework.stereotype.Service

@Service
class WaitQueueService(
    private val waitQueueRedisRepository: WaitQueueRedisRepository,
) {
    fun register(job: Job): JobResponse {
        val rank = waitQueueRedisRepository.save(job) + 1
        return JobResponse(JobStatus.WAIT, WaitQueueInfo(rank * job.waitTimePerOneJob, rank.toInt()))
    }

    fun isMember(job: Job): Boolean {
        return waitQueueRedisRepository.isMember(job)
    }

    fun retrieve(job: Job): JobResponse {
        if (!waitQueueRedisRepository.isMember(job)) {
            return JobResponse(JobStatus.TIMEOUT)
        }
        waitQueueRedisRepository.updateUserTtl(job)

        val rank = (waitQueueRedisRepository.rank(job)) + 1
        return JobResponse(JobStatus.WAIT, WaitQueueInfo(rank * job.waitTimePerOneJob, rank.toInt()))
    }

    fun size(eventId: String): Long {
        return waitQueueRedisRepository.size(eventId)
    }

    fun remove(job: Job) {
        waitQueueRedisRepository.remove(job)
    }

    fun findJobsBy(eventId: String, size: Long): List<Job> {
        return waitQueueRedisRepository.popMin(eventId, size)
    }

    fun getWaitQueueSizes(eventIds: List<String>): List<QueueSize> {
        val sizes = waitQueueRedisRepository.sizes(eventIds)
        println(sizes)
        return sizes
    }

    fun findJobsBy(queueInfos: List<QueueInfo>): List<Job> {
        return waitQueueRedisRepository.popMin(queueInfos)
    }
}