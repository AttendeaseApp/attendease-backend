package com.attendease.backend.student.service.event.retrieval.impl;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.student.service.event.retrieval.EventRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventRetrievalServiceImpl implements EventRetrievalService {

    private final EventRepository eventSessionRepository;

    @Override
    @Cacheable(value = "events", key = "#id")
    public Optional<Event> getEventById(String id) {
        log.debug("Fetching event from database: {}", id);
        return eventSessionRepository.findById(id);
    }

    @Override
    @Cacheable(value = "homepage-events", unless = "#result.isEmpty()")
    public List<Event> getOngoingRegistrationAndActiveEvents() {
        long startTime = System.currentTimeMillis();
        log.debug("Fetching homepage events from database");
        List<Event> events = eventSessionRepository.findByEventStatusIn(
                Arrays.asList(EventStatus.ONGOING, EventStatus.UPCOMING, EventStatus.REGISTRATION)
        );
        long duration = System.currentTimeMillis() - startTime;
        log.info("Fetched {} events in {}ms", events.size(), duration);
        return events;
    }

    @CacheEvict(value = "homepage-events", allEntries = true)
    public void clearHomepageEventsCache() {
        log.info("Cleared homepage events cache");
    }
}
