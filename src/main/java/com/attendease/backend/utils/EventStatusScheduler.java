package com.attendease.backend.utils;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventSessionsRepository eventSessionRepository;

    @Scheduled(fixedRate = 15000)
    public void updateEventStatuses() {
        try {
            LocalDateTime now = LocalDateTime.now();

            List<EventSessions> events = eventSessionRepository.findByEventStatusIn(Arrays.asList(EventStatus.UPCOMING, EventStatus.REGISTRATION, EventStatus.ONGOING));

            boolean updated = false;

            for (EventSessions event : events) {
                LocalDateTime registrationStart = event.getTimeInRegistrationStartDateTime();
                LocalDateTime start = event.getStartDateTime();
                LocalDateTime end = event.getEndDateTime();

                if (registrationStart == null || start == null || end == null) continue;

                EventStatus currentStatus = event.getEventStatus();
                EventStatus newStatus = null;

                if (now.isBefore(registrationStart)) {
                    newStatus = EventStatus.UPCOMING;
                } else if ((now.isEqual(registrationStart) || now.isAfter(registrationStart)) && now.isBefore(start)) {
                    newStatus = EventStatus.REGISTRATION;
                } else if ((now.isEqual(start) || now.isAfter(start)) && now.isBefore(end)) {
                    newStatus = EventStatus.ONGOING;
                } else if (now.isEqual(end) || now.isAfter(end)) {
                    newStatus = EventStatus.CONCLUDED;
                }

                if (newStatus != null && currentStatus != newStatus) {
                    event.setEventStatus(newStatus);
                    updated = true;
                    log.info("Event {} status updated from {} to {}", event.getEventId(), currentStatus, newStatus);
                }
            }

            if (updated) {
                eventSessionRepository.saveAll(events);
            }

            log.info("Checked {} events for status updates", events.size());
        } catch (Exception e) {
            log.error("Error updating event statuses: {}", e.getMessage(), e);
        }
    }
}
