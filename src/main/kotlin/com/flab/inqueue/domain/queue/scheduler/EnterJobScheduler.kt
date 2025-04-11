package com.flab.inqueue.domain.queue.scheduler

import com.flab.inqueue.domain.event.entity.Event
import com.flab.inqueue.domain.event.repository.EventRepository
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EnterJobScheduler(
    private val eventRepository: EventRepository,
    private val enterJobScheduleTask: EnterJobScheduleTask
) {
    // @Scheduled(fixedRate = 1000)
    fun execute() {
        val events = eventRepository.findOngoingEvents(LocalDateTime.now())

        for (event in events) {
            enterJobScheduleTask.enterJobs(event)
        }
    }

    @Scheduled(fixedRate = 1000)
    fun executeInChunks() {
        var pageNumber = 0
        val pageSize = 100
        var events: List<Event>
        do {
            val pageable = PageRequest.of(pageNumber, pageSize)
            events = eventRepository.findOngoingEventsByPage(LocalDateTime.now(), pageable)
            enterJobScheduleTask.enterJobs(events)
            pageNumber++
        } while (events.isNotEmpty())
    }
}