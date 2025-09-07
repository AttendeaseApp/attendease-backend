package com.attendease.backend.eventSessionManagement.service.status.impl;

import com.attendease.backend.eventSessionManagement.repository.EventSessionRepositoryInterface;
import com.attendease.backend.model.enums.EventStatus;
import com.attendease.backend.model.events.EventSessions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class EventStatusScheduler {

    private final EventSessionRepositoryInterface eventSessionRepository;

    public EventStatusScheduler(EventSessionRepositoryInterface eventSessionRepository) {
        this.eventSessionRepository = eventSessionRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void updateEventStatuses() {
        try {
            Date now = new Date();
            List<EventSessions> activeEvents = eventSessionRepository.findByStatus(EventStatus.ACTIVE);
            List<EventSessions> registrationEvents = eventSessionRepository.findByStatus(EventStatus.REGISTRATION);
            List<EventSessions> ongoingEvents = eventSessionRepository.findByStatus(EventStatus.ONGOING);

            activeEvents.addAll(registrationEvents);
            activeEvents.addAll(ongoingEvents);

            for (EventSessions event : activeEvents) {
                Date start = event.getStartDateTime();
                Date end = event.getEndDateTime();

                if (start != null && end != null) {
                    long startTimeMillis = start.getTime();
                    long nowMillis = now.getTime();
                    // 30 minutes before start
                    long registrationStartMillis = startTimeMillis - (30 * 60 * 1000);
                    if (nowMillis >= registrationStartMillis && nowMillis < startTimeMillis) {
                        // REGISTRATION
                        if (event.getEventStatus() != EventStatus.REGISTRATION) {
                            event.setEventStatus(EventStatus.REGISTRATION);
                            eventSessionRepository.save(event);
                            log.info("Event {} status updated to REGISTRATION", event.getEventId());
                        }
                    } else if (nowMillis >= startTimeMillis && nowMillis < end.getTime()) {
                        // ONGOING
                        if (event.getEventStatus() != EventStatus.ONGOING) {
                            event.setEventStatus(EventStatus.ONGOING);
                            eventSessionRepository.save(event);
                            log.info("Event {} status updated to ONGOING", event.getEventId());
                        }
                    } else if (nowMillis >= end.getTime()) {
                        // CONCLUDED
                        if (event.getEventStatus() != EventStatus.CONCLUDED) {
                            event.setEventStatus(EventStatus.CONCLUDED);
                            eventSessionRepository.save(event);
                            log.info("Event {} status updated to CONCLUDED", event.getEventId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error updating event statuses: {}", e.getMessage(), e);
        }
    }


}

