package com.flab.inqueue.domain.queue.scheduler

import com.flab.inqueue.domain.event.entity.Event
import com.flab.inqueue.domain.queue.service.JobService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class EnterJobScheduleTask(
    private val jobService: JobService,
) {

    @Async("threadPoolTaskExecutor")
    fun enterJobs(event: Event) {
        // event 중 대기열 사이즈가 0 이 아닌 이벤트 필터링
        if (jobService.getWaitQueueSize(event) == 0L) {
            return
        }

        // event 중 작업열에 작업이 들어갈 여유가 있는지 필터링
        val availableJobQueueSize = event.jobQueueSize - jobService.getJobQueueSize(event)
        if (availableJobQueueSize <= 0) {
            return
        }

        jobService.enterAll(event, availableJobQueueSize)
    }

    @Async("threadPoolTaskExecutor")
    fun enterJobs(events: List<Event>) {
        jobService.enterAll(events)
    }
}