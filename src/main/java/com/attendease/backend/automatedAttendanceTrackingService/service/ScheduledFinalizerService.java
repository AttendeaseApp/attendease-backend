package com.attendease.backend.automatedAttendanceTrackingService.service;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledFinalizerService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AutomatedAttendanceFinalizer automatedAttendanceFinalizer;

    @Scheduled(fixedRate = 60000)
    public void runScheduledFinalization() {
        try {
            List<EventSessions> concludedEvents = eventSessionsRepository.findByEventStatus(EventStatus.CONCLUDED);
            for (EventSessions event : concludedEvents) {
                log.info("Finalizing attendance for event: {}", event.getId());
                automatedAttendanceFinalizer.finalizeAttendanceForEvent(event);
                event.setEventStatus(EventStatus.FINALIZED);
                eventSessionsRepository.save(event);
            }
        } catch (Exception e) {
            log.error("Error during scheduled attendance finalization", e);
        }
    }
}


