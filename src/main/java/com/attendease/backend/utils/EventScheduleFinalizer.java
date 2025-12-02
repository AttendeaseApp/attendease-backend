package com.attendease.backend.utils;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventScheduleFinalizer {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsFinalizer attendanceRecordsFinalizer;

    @Scheduled(fixedRate = 15000)
    public void runScheduledFinalization() {
        try {
            List<EventSessions> concludedEvents = eventSessionsRepository.findByEventStatus(EventStatus.CONCLUDED);
            for (EventSessions event : concludedEvents) {
                if (event.getEventStatus() == EventStatus.CANCELLED) {
                    log.info("Skipping finalization for cancelled event: {} {}", event.getEventId(), event.getEventName());
                    continue;
                }
                log.info("Finalizing attendance records and status for event: {} {}", event.getEventId(), event.getEventName());
                attendanceRecordsFinalizer.finalizeAttendanceForEvent(event);
                event.setEventStatus(EventStatus.FINALIZED);
                eventSessionsRepository.save(event);
            }
        } catch (Exception e) {
            log.error("Error during scheduled attendance finalization", e);
        }
    }
}
