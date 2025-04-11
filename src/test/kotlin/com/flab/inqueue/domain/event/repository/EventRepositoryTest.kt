package com.flab.inqueue.domain.event.repository

import com.flab.inqueue.domain.member.entity.Member
import com.flab.inqueue.domain.member.entity.MemberKey
import com.flab.inqueue.domain.member.repository.MemberRepository
import com.flab.inqueue.fixture.createEventRequest
import com.flab.inqueue.support.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*

@RepositoryTest
class EventRepositoryTest(
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository
) {

    lateinit var member: Member

    @BeforeEach
    fun setUp() {
        member = Member(name = "testMember", key = MemberKey("testClientId", "testClientSecret"))
        memberRepository.save(member)
    }

    @DisplayName("이벤트Id로 이벤트를 조회한다")
    @Test
    fun retrieve() {
        // given
        val eventId = UUID.randomUUID().toString()
        val event = createEventRequest().toEntity(eventId, member)
        eventRepository.save(event)

        // when
        val foundEvent = eventRepository.findByEventId(eventId)

        //then
        assertAll(
            { assertThat(foundEvent).isNotNull },
            { assertThat(foundEvent!!.eventId).isEqualTo(eventId) },
            { assertThat(foundEvent!!.jobQueueSize).isNotNull },
            { assertThat(foundEvent!!.jobQueueLimitTime).isNotNull },
            { assertThat(foundEvent!!.period).isNotNull }
        )
    }

    @DisplayName("고객사 ClientId로 이벤트를 조횧한다.")
    @Test
    fun retrieveAll() {
        // given
        val eventId1 = UUID.randomUUID().toString()
        val event1 = createEventRequest().toEntity(eventId1, member)
        val eventId2 = UUID.randomUUID().toString()
        val event2 = createEventRequest().toEntity(eventId2, member)
        eventRepository.saveAll(listOf(event1, event2))

        // when
        val events = eventRepository.findAllByMemberKeyClientId(member.key.clientId)

        // then
        assertThat(events).hasSize(2)
            .extracting("eventId")
            .contains(eventId1, eventId2)
    }

    @Test
    @DisplayName("현재 진행중인 이벤트를 조회한다.")
    fun findOngoingEvents() {
        //given
        val eventId1 = UUID.randomUUID().toString()
        val event1 = createEventRequest(
            waitQueueStartDateTime = LocalDateTime.of(2025, 3, 26, 10, 0),
            waitQueueEndDateTime = LocalDateTime.of(2025, 3, 26, 12, 0)
        ).toEntity(eventId1, member)

        val eventId2 = UUID.randomUUID().toString()
        val event2 = createEventRequest(
            waitQueueStartDateTime = LocalDateTime.of(2025, 3, 26, 10, 0),
            waitQueueEndDateTime = LocalDateTime.of(2025, 3, 26, 14, 0)
        ).toEntity(eventId2, member)

        eventRepository.saveAll(listOf(event1, event2))

        // when
        val findOngoingEvents = eventRepository.findOngoingEvents(LocalDateTime.of(2025, 3, 26, 12, 1))

        // then
        assertThat(findOngoingEvents).hasSize(1)
            .extracting("eventId")
            .contains(eventId2)
    }

    @Test
    @DisplayName("현재 진행중인 이벤트를 페이지 단위로 조회한다.")
    fun findOngoingEventsByPage() {
        //given
        val eventId1 = UUID.randomUUID().toString()
        val event1 = createEventRequest(
            waitQueueStartDateTime = LocalDateTime.of(2025, 3, 26, 10, 0),
            waitQueueEndDateTime = LocalDateTime.of(2025, 3, 26, 12, 0)
        ).toEntity(eventId1, member)

        val eventId2 = UUID.randomUUID().toString()
        val event2 = createEventRequest(
            waitQueueStartDateTime = LocalDateTime.of(2025, 3, 26, 10, 0),
            waitQueueEndDateTime = LocalDateTime.of(2025, 3, 26, 14, 0)
        ).toEntity(eventId2, member)

        eventRepository.saveAll(listOf(event1, event2))

        // when
        var findOngoingEvents = eventRepository.findOngoingEventsByPage(
            LocalDateTime.of(2025, 3, 26, 11, 0),
            PageRequest.of(0, 1)
        )

        findOngoingEvents = findOngoingEvents + eventRepository.findOngoingEventsByPage(
            LocalDateTime.of(2025, 3, 26, 11, 0),
            PageRequest.of(1, 1)
        )

        // then
        assertThat(findOngoingEvents).hasSize(2)
            .extracting("eventId")
            .containsExactlyInAnyOrder(eventId1, eventId2)
    }
}