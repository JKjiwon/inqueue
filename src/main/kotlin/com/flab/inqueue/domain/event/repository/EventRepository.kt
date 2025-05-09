package com.flab.inqueue.domain.event.repository

import com.flab.inqueue.domain.event.entity.Event
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface EventRepository : JpaRepository<Event, Long> {

    fun findByEventId(eventId: String): Event?

    fun findAllByMemberKeyClientId(clientId: String): List<Event>

    @Query("select e from Event e where e.period.startDateTime <= :baseTime and e.period.endDateTime >= :baseTime")
    fun findOngoingEvents(baseTime: LocalDateTime): List<Event>

    @Query("select e from Event e where e.period.startDateTime <= :baseTime and e.period.endDateTime >= :baseTime")
    fun findOngoingEventsByPage(baseTime: LocalDateTime, pageable: Pageable): List<Event>
}