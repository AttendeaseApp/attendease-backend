package com.attendease.backend.eventSessionManagement.service;

import com.attendease.backend.eventSessionManagement.repository.EventSessionRepository;
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

    private final EventSessionRepository eventSessionRepository;

    public EventStatusScheduler(EventSessionRepository eventSessionRepository) {
        this.eventSessionRepository = eventSessionRepository;
    }

    @Scheduled(fixedRate = 10000)
    public void updateEventStatuses() {
        try {
            Date now = new Date();
            List<EventSessions> activeEvents = eventSessionRepository.findByStatus(EventStatus.ACTIVE);
            List<EventSessions> ongoingEvents = eventSessionRepository.findByStatus(EventStatus.ONGOING);

            activeEvents.addAll(ongoingEvents);

            for (EventSessions event : activeEvents) {
                Date start = event.getStartDateTime();
                Date end = event.getEndDateTime();

                if (start != null && end != null) {
                    if (now.after(start) && now.before(end)) {
                        // should be ONGOING
                        if (event.getEventStatus() != EventStatus.ONGOING) {
                            event.setEventStatus(EventStatus.ONGOING);
                            eventSessionRepository.save(event);
                            log.info("Event {} status updated to ONGOING", event.getEventId());
                        }
                    } else if (now.after(end)) {
                        // should be CONCLUDED
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

