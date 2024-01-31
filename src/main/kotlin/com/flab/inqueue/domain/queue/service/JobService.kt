package com.flab.inqueue.domain.queue.service

import com.flab.inqueue.domain.event.entity.Event
import com.flab.inqueue.domain.event.repository.EventRepository
import com.flab.inqueue.domain.queue.dto.JobResponse
import com.flab.inqueue.domain.queue.dto.JobVerificationResponse
import com.flab.inqueue.domain.queue.entity.Job
import com.flab.inqueue.domain.queue.entity.JobStatus
import com.flab.inqueue.domain.queue.repository.JobRedisRepository
import com.flab.inqueue.exception.ApplicationException
import com.flab.inqueue.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class JobService(
    private val jobRedisRepository: JobRedisRepository,
    private val eventRepository: EventRepository,
    private val waitQueueService: WaitQueueService,
) {
    fun enter(eventId: String, userId: String): JobResponse {
        val event = findEvent(eventId)

        if (isEnterJob(event)) {
            val job = Job(eventId, userId, JobStatus.ENTER, event.jobQueueLimitTime)
            jobRedisRepository.register(job)
            return JobResponse(job.status)
        }

        val waitJob = Job(
            eventId = eventId,
            userId = userId,
            jobQueueSize = event.jobQueueSize,
            queueLimitTime = event.jobQueueLimitTime
        )
        return waitQueueService.register(waitJob)
    }

    fun enterAll(event: Event, size: Long) {
        val waitJobs: List<Job> = waitQueueService.getJobsBySize(event.eventId, size)
        val enterJobs = waitJobs.map { it.enter(event.jobQueueLimitTime) }
        jobRedisRepository.registerAll(enterJobs)
    }

    fun retrieve(eventId: String, userId: String): JobResponse {
        val job = Job(eventId, userId, JobStatus.ENTER)
        if (jobRedisRepository.isMember(job)) {
            return JobResponse(JobStatus.ENTER)
        }

        val event = findEvent(eventId)
        val waitJob = Job(
            eventId = eventId,
            userId = userId,
            jobQueueSize = event.jobQueueSize,
            queueLimitTime = event.jobQueueLimitTime
        )
        return waitQueueService.retrieve(waitJob)
    }

    fun verify(eventId: String, clientId: String, userId: String): JobVerificationResponse {
        val event = findEvent(eventId)
        if (!event.isAccessible(clientId)) {
            throw ApplicationException.of(ErrorCode.EVENT_NOT_ACCESSED)
        }

        val job = Job(eventId, userId, JobStatus.ENTER)
        val isVerified = jobRedisRepository.isMember(job)
        return JobVerificationResponse(isVerified)
    }

    fun close(eventId: String, clientId: String, userId: String) {
        val event = findEvent(eventId)
        if (!event.isAccessible(clientId)) {
            throw ApplicationException.of(ErrorCode.EVENT_NOT_ACCESSED)
        }

        val job = Job(eventId, userId, JobStatus.ENTER)
        if (!jobRedisRepository.isMember(job)) {
            throw ApplicationException.of(ErrorCode.JOB_NOT_FOUND_IN_JOB_QUEUE)
        }
        jobRedisRepository.remove(job)
    }

    fun getJobQueueSize(event: Event): Long {
        return jobRedisRepository.size(JobStatus.ENTER.makeRedisKey(event.eventId))
    }

    fun getWaitQueueSize(event: Event): Long {
        return waitQueueService.size(JobStatus.WAIT.makeRedisKey(event.eventId))
    }

    private fun findEvent(eventId: String): Event {
        return eventRepository.findByEventId(eventId) ?: throw ApplicationException.of(ErrorCode.EVENT_NOT_FOUND)
    }

    private fun isEnterJob(event: Event): Boolean {
        val waitQueueSize = getWaitQueueSize(event)
        val jobQueueSize = getJobQueueSize(event)

        return waitQueueSize == 0L && jobQueueSize < event.jobQueueSize
    }
}