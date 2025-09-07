package com.attendease.backend.attendanceTrackingService.service.AttendanceFinalizerService;

import com.attendease.backend.eventAttendanceMonitoringService.repository.EventRepositoryInterface;
import com.attendease.backend.eventSessionManagement.repository.EventSessionRepositoryInterface;
import com.attendease.backend.model.enums.AttendanceStatus;
import com.attendease.backend.model.enums.EventStatus;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.records.AttendanceRecords;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AttendanceFinalizerService {

    private final EventSessionRepositoryInterface eventSessionRepository;
    private final EventRepositoryInterface eventRepositoryInterface;

    public AttendanceFinalizerService(EventSessionRepositoryInterface eventSessionRepository, EventRepositoryInterface eventRepositoryInterface) {
        this.eventSessionRepository = eventSessionRepository;
        this.eventRepositoryInterface = eventRepositoryInterface;
    }

    @Scheduled(fixedRate = 60000) //every 60 seconds
    public void finalizeAttendances() {
        try {
            List<EventSessions> concludedEvents = eventSessionRepository.findByStatus(EventStatus.CONCLUDED);
            for (EventSessions event : concludedEvents) {
                String eventId = event.getEventId();
                List<AttendanceRecords> records = eventRepositoryInterface.getAttendanceRecords(eventId);

                for (AttendanceRecords record : records) {
                    AttendanceStatus currentStatus = record.getAttendanceStatus();
                    AttendanceStatus evaluatedStatus = evaluateAttendanceAfterEventEnds(event, record);

                    if (evaluatedStatus == null || evaluatedStatus == currentStatus) {
                        continue;
                    }

                    record.setTimeOut(new Date());
                    record.setAttendanceStatus(evaluatedStatus);
                    eventRepositoryInterface.saveAttendanceRecord(record);
                    log.info("Attendance finalized for student {} in event {}: {}",
                            record.getStudentNumberRefId().getId(), eventId, evaluatedStatus);
                }
            }
        } catch (Exception e) {
            log.error("Error during attendance finalization: {}", e.getMessage(), e);
        }
    }


    private AttendanceStatus evaluateAttendanceAfterEventEnds(EventSessions event, AttendanceRecords record) {
        if (record.getTimeIn() == null) {
            record.setReason("Did not check in");
            return AttendanceStatus.ABSENT;
        }
        if (!record.getTimeIn().before(event.getStartDateTime())) {
            record.setReason("Late check-in");
            return AttendanceStatus.ABSENT;
        }
        record.setReason(null);
        return AttendanceStatus.PRESENT;
    }

}

