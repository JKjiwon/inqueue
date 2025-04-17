package com.flab.inqueue.domain.queue.scheduler

import com.flab.inqueue.domain.event.entity.Event
import com.flab.inqueue.domain.queue.service.JobService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class EnterJobScheduleTask(
    private val jobService: JobService,
) {

    @Deprecated("현재 사용하지 않는 함수 입니다. 변경된 로직과 테스트 후 삭제 예정입니다.")
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

        jobService.enqueue(event, availableJobQueueSize)
    }

    @Async("threadPoolTaskExecutor")
    fun enterJobs(events: List<Event>) {
        jobService.enqueue(events)
    }
}