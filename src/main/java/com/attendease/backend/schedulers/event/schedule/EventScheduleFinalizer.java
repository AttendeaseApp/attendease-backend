package com.attendease.backend.schedulers.event.schedule;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.repository.event.EventRepository;
import java.util.List;

import com.attendease.backend.schedulers.utils.attendance.records.AttendanceRecordsFinalizer;
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

    @Scheduled(fixedRate = 15000)
    public void runScheduledFinalization() throws Exception {
        try {
            List<Event> concludedEvents = eventRepository.findByEventStatus(EventStatus.CONCLUDED);
            for (Event event : concludedEvents) {
                if (event.getEventStatus() == EventStatus.CANCELLED) {
                    log.info("Skipping finalization for cancelled event: {} {}", event.getEventId(), event.getEventName());
                    continue;
                }
                log.info("Finalizing attendance records and status for event: {} {}", event.getEventId(), event.getEventName());
                attendanceRecordsFinalizer.finalizeAttendanceForEvent(event);
                event.setEventStatus(EventStatus.FINALIZED);
                eventRepository.save(event);
            }
        } catch (Exception e) {
            throw new Exception("Error during scheduled attendance finalization" + e);
        }
    }
}
