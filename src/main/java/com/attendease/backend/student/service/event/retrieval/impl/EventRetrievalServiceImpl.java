package com.attendease.backend.student.service.event.retrieval.impl;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.repository.event.EventRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.attendease.backend.student.service.event.retrieval.EventRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventRetrievalServiceImpl implements EventRetrievalService {

    private final EventRepository eventSessionRepository;

    @Override
    public Optional<Event> getEventById(String id) {
        return eventSessionRepository.findById(id);
    }

    @Override
    public List<Event> getOngoingRegistrationAndActiveEvents() {
        return eventSessionRepository.findByEventStatusIn(Arrays.asList(EventStatus.ONGOING, EventStatus.UPCOMING, EventStatus.REGISTRATION));
    }
}
