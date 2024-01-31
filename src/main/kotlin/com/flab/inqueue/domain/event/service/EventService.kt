package com.flab.inqueue.domain.event.service

import com.flab.inqueue.domain.event.dto.EventRequest
import com.flab.inqueue.domain.event.dto.EventResponse
import com.flab.inqueue.domain.event.dto.EventRetrieveResponse
import com.flab.inqueue.domain.event.entity.Event
import com.flab.inqueue.domain.event.repository.EventRepository
import com.flab.inqueue.domain.member.repository.MemberRepository
import com.flab.inqueue.exception.ApplicationException
import com.flab.inqueue.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class EventService(
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository
) {
    fun retrieve(clientId: String, eventId: String): EventRetrieveResponse {
        val foundEvent = findEvent(eventId)
        if (!foundEvent.isAccessible(clientId)) {
            throw ApplicationException.of(ErrorCode.EVENT_NOT_ACCESSED)
        }

        return toEventRetrieveResponse(foundEvent)
    }

    fun retrieveAll(customId: String): List<EventRetrieveResponse> {
        return eventRepository.findAllByMemberKeyClientId(customId).map { toEventRetrieveResponse(it) }
    }

    @Transactional
    fun create(clientId: String, request: EventRequest): EventResponse {
        val member = memberRepository.findByKeyClientId(clientId)
            ?: throw ApplicationException.of(ErrorCode.MEMBER_NOT_FOUND)
        val eventId = UUID.randomUUID().toString()
        val savedEvent = eventRepository.save(request.toEntity(eventId, member))
        return EventResponse(savedEvent.eventId)
    }

    @Transactional
    fun update(clientId: String, eventId: String, request: EventRequest) {
        val foundEvent = findEvent(eventId)
        if (!foundEvent.isAccessible(clientId)) {
            throw ApplicationException.of(ErrorCode.EVENT_NOT_ACCESSED)
        }
        foundEvent.update(request.toEntity(eventId, foundEvent.member))
    }

    @Transactional
    fun delete(clientId: String, eventId: String) {
        val foundEvent = findEvent(eventId)
        if (!foundEvent.isAccessible(clientId)) {
            throw ApplicationException.of(ErrorCode.EVENT_NOT_ACCESSED)
        }
        eventRepository.deleteById(foundEvent.id!!)
    }

    private fun findEvent(eventId: String): Event {
        return eventRepository.findByEventId(eventId)
            ?: throw ApplicationException.of(ErrorCode.EVENT_NOT_FOUND)
    }

    private fun toEventRetrieveResponse(event: Event): EventRetrieveResponse {
        return EventRetrieveResponse(
            eventId = event.eventId,
            waitQueueStartTime = event.period.startDateTime,
            waitQueueEndTime = event.period.endDateTime,
            jobQueueSize = event.jobQueueSize,
            jobQueueLimitTime = event.jobQueueLimitTime,
            redirectUrl = event.redirectUrl
        )
    }
}