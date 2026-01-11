package com.attendease.backend.schedulers.event.schedule;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.schedulers.attendance.records.AttendanceRecordsFinalizer;
import com.attendease.backend.student.controller.event.retrieval.EventBroadcastService;
import com.attendease.backend.student.service.event.retrieval.impl.EventRetrievalServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventScheduleFinalizer {

    private final EventRepository eventRepository;
    private final AttendanceRecordsFinalizer attendanceRecordsFinalizer;
    private final EventBroadcastService eventBroadcastService;
    private final EventRetrievalServiceImpl eventRetrievalService;

    @Scheduled(fixedRate = 15000)
    public void runScheduledFinalization() throws Exception {
        try {
            List<Event> concludedEvents = eventRepository.findByEventStatus(EventStatus.CONCLUDED);
            boolean anyFinalized = false;
            for (Event event : concludedEvents) {
                if (event.getEventStatus() == EventStatus.CANCELLED) {
                    log.info("Skipping finalization for cancelled event: {} {}", event.getEventId(), event.getEventName());
                    continue;
                }
                log.info("Finalizing attendance records and status for event: {} {}", event.getEventId(), event.getEventName());
                attendanceRecordsFinalizer.finalizeAttendanceForEvent(event);
                event.setEventStatus(EventStatus.FINALIZED);
                eventRepository.save(event);
                anyFinalized = true;
                log.info("Event {} finalized successfully", event.getEventId());
            }
            if (anyFinalized) {
                eventRetrievalService.clearHomepageEventsCache();
                log.info("Cache cleared after event finalization");
                eventBroadcastService.triggerImmediateBroadcast();
                log.info("Triggered immediate broadcast after event finalization");
            }
        } catch (Exception e) {
            throw new Exception("Error during scheduled attendance finalization: " + e.getMessage(), e);
        }
    }
}
