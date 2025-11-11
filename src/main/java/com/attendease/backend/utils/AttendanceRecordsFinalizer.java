package com.attendease.backend.utils;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.EventCheckIn.AttendancePingLogs;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceRecordsFinalizer {

    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final StudentRepository studentRepository;

    /**
     * Re-evaluates and finalizes attendance based on ping logs.
     * A student is marked PRESENT if they were inside for at least 70% of the event duration.
     */
    public void finalizeAttendanceForEvent(EventSessions event) {
        String eventId = event.getEventId();
        String eventName = event.getEventName();

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository.findByEventEventId(eventId);
        List<Students> expectedStudents = getExpectedStudentsForEvent(event);
        Set<String> studentsWithRecords = attendanceRecords.stream().map(r -> r.getStudent().getId()).collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        for (AttendanceRecords record : attendanceRecords) {
            AttendanceStatus oldStatus = record.getAttendanceStatus();
            AttendanceStatus finalStatus = evaluateAttendanceFromLogs(event, record);

            if (finalStatus != oldStatus) {
                record.setAttendanceStatus(finalStatus);
                record.setTimeOut(now);
                attendanceRecordsRepository.save(record);
                log.info("Updated student {} to {} for event {}", record.getStudent().getStudentNumber(), finalStatus, eventName);
            }
        }

        // mark missing students as ABSENT
        for (Students student : expectedStudents) {
            if (!studentsWithRecords.contains(student.getId())) {
                AttendanceRecords absentRecord = AttendanceRecords.builder()
                        .student(student)
                        .event(event)
                        .attendanceStatus(AttendanceStatus.ABSENT)
                        .reason("No presence detected at all on this event")
                        .timeIn(null)
                        .timeOut(null)
                        .build();
                attendanceRecordsRepository.save(absentRecord);
                log.info("Marked ABSENT for missing student {} in event {}, {}", student.getStudentNumber(), eventId, eventName);
            }
        }
        log.info("Attendance finalization completed for event {}, {}", eventId, eventName);
    }

    /**
     * Uses attendance ping logs to decide the student's final attendance.
     */
    private AttendanceStatus evaluateAttendanceFromLogs(EventSessions event, AttendanceRecords record) {
        List<AttendancePingLogs> pings = record.getAttendancePingLogs();
        if (pings == null || pings.isEmpty()) {
            record.setReason("No location pings recorded");
            return AttendanceStatus.ABSENT;
        }

        long eventStart = event.getStartDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long eventEnd = event.getEndDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long eventDuration = eventEnd - eventStart;
        long insideTime = computeInsideDuration(pings, eventStart, eventEnd);

        double insideRatio = (double) insideTime / eventDuration;
        log.info("Student {} inside {}% of event {}", record.getStudent().getStudentNumber(), insideRatio * 100, event.getEventName());

        if (insideRatio >= 0.7) {
            record.setReason(null);
            return AttendanceStatus.PRESENT;
        } else if (insideRatio >= 0.3) {
            record.setReason("Student was present only for part of the event");
            return AttendanceStatus.IDLE;
        } else {
            record.setReason("Student was outside for most of the event");
            return AttendanceStatus.ABSENT;
        }
    }

    /**
     * Estimates how long (in ms) the student was inside based on pings.
     * This assumes pings are roughly evenly spaced in time.
     */
    private long computeInsideDuration(List<AttendancePingLogs> pings, long eventStart, long eventEnd) {
        if (pings.size() < 2) return 0;

        pings.sort(Comparator.comparingLong(AttendancePingLogs::getTimestamp));

        long totalInside = 0;

        for (int i = 0; i < pings.size() - 1; i++) {
            AttendancePingLogs current = pings.get(i);
            AttendancePingLogs next = pings.get(i + 1);

            long t1 = Math.max(current.getTimestamp(), eventStart);
            long t2 = Math.min(next.getTimestamp(), eventEnd);

            if (current.isInside()) {
                totalInside += (t2 - t1);
            }
        }

        return totalInside;
    }

    private List<Students> getExpectedStudentsForEvent(EventSessions event) {
        //TODO:eligible students
        return studentRepository.findAll();
    }
}
