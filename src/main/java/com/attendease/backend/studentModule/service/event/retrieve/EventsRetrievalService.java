package com.attendease.backend.studentModule.service.event.retrieve;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventsRetrievalService {

    private final EventSessionsRepository eventSessionRepository;

    public List<EventSessions> getOngoingRegistrationAndActiveEvents() {
        return eventSessionRepository.findByEventStatusIn(Arrays.asList(EventStatus.ONGOING, EventStatus.UPCOMING, EventStatus.REGISTRATION));
    }
}
