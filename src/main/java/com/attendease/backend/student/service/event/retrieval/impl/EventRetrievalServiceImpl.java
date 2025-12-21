package com.attendease.backend.student.service.event.retrieval.impl;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.attendease.backend.student.service.event.retrieval.EventRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventRetrievalServiceImpl implements EventRetrievalService {

    private final EventSessionsRepository eventSessionRepository;

    @Override
    public Optional<EventSessions> getEventById(String id) {
        return eventSessionRepository.findById(id);
    }

    @Override
    public List<EventSessions> getOngoingRegistrationAndActiveEvents() {
        return eventSessionRepository.findByEventStatusIn(Arrays.asList(EventStatus.ONGOING, EventStatus.UPCOMING, EventStatus.REGISTRATION));
    }
}
