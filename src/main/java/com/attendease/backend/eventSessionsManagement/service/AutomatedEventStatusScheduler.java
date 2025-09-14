package com.attendease.backend.eventSessionsManagement.service;

import com.attendease.backend.model.enums.EventStatus;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutomatedEventStatusScheduler {

    private final EventSessionsRepository eventSessionRepository;

    @Scheduled(fixedRate = 60000)
    public void updateEventStatuses() {
        try {
            Date now = new Date();
            List<EventSessions> events = eventSessionRepository.findByEventStatusIn(
                    Arrays.asList(EventStatus.ACTIVE, EventStatus.REGISTRATION, EventStatus.ONGOING)
            );

            boolean updated = false;

            for (EventSessions event : events) {
                Date start = event.getStartDateTime();
                Date end = event.getEndDateTime();

                if (start != null && end != null) {
                    long startTimeMillis = start.getTime();
                    long nowMillis = now.getTime();
                    long registrationStartMillis = startTimeMillis - (30 * 60 * 1000);

                    EventStatus currentStatus = event.getEventStatus();
                    EventStatus newStatus = null;

                    if (nowMillis >= registrationStartMillis && nowMillis < startTimeMillis) {
                        newStatus = EventStatus.REGISTRATION;
                    } else if (nowMillis >= startTimeMillis && nowMillis < end.getTime()) {
                        newStatus = EventStatus.ONGOING;
                    } else if (nowMillis >= end.getTime()) {
                        newStatus = EventStatus.CONCLUDED;
                    }

                    if (newStatus != null && currentStatus != newStatus) {
                        event.setEventStatus(newStatus);
                        updated = true;
                        log.info("Event {} status updated to {}", event.getEventId(), newStatus);
                    }
                    log.info("Found {} events with ACTIVE, REGISTRATION, or ONGOING status", events.size());
                }
            }

            if (updated) {
                eventSessionRepository.saveAll(events);
            }

        } catch (Exception e) {
            log.error("Error updating event statuses: {}", e.getMessage(), e);
        }
    }
}


