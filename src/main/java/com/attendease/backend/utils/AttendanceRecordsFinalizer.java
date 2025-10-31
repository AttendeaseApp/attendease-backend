package com.attendease.backend.utils;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceRecordsFinalizer {

    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final StudentRepository studentRepository;

    public void finalizeAttendanceForEvent(EventSessions event) {
        String eventId = event.getEventId();
        String eventName =  event.getEventName();

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository.findByEventEventId(eventId);
        List<Students> expectedStudents = getExpectedStudentsForEvent(event);
        Set<String> studentsWithRecords = attendanceRecords.stream().map(record -> record.getStudent().getId()).collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        for (AttendanceRecords record : attendanceRecords) {
            AttendanceStatus currentStatus = record.getAttendanceStatus();
            AttendanceStatus finalStatus = evaluateAttendanceAfterEventEnds(event, record);

            if (finalStatus != null && finalStatus != currentStatus) {
                record.setAttendanceStatus(finalStatus);
                record.setTimeOut(now);
                attendanceRecordsRepository.save(record);
                log.info("Updated student {} -> {} for event {}", record.getStudent().getStudentNumber(), finalStatus, eventId);
            }
        }

        for (Students student : expectedStudents) {
            if (!studentsWithRecords.contains(student.getId())) {
                AttendanceRecords absentRecord = AttendanceRecords.builder()
                        .student(student)
                        .event(event)
                        .attendanceStatus(AttendanceStatus.ABSENT)
                        .reason("Did not check in for the event: " + event.getEventName())
                        .timeIn(null)
                        .timeOut(null)
                        .build();
                attendanceRecordsRepository.save(absentRecord);
                log.info("Marked ABSENT for missing student {} in event {}, {}", student.getStudentNumber(), eventId, eventName);
            }
        }
        log.info("Attendance finalization completed for event {}, {}", eventId, eventName);
    }


    private AttendanceStatus evaluateAttendanceAfterEventEnds(EventSessions event, AttendanceRecords record) {
        LocalDateTime eventStart = event.getStartDateTime();
        LocalDateTime timeIn = record.getTimeIn();
        AttendanceStatus current = record.getAttendanceStatus(); // use current status

        // if the student never checked in
        if (timeIn == null) {
            record.setReason("Did not check in for the event: " + event.getEventName());
            return AttendanceStatus.ABSENT;
        }

        // if the student registered late in late
        if (timeIn.isAfter(eventStart)) {
            record.setReason("Late registration for the event: " + event.getEventName());
            return AttendanceStatus.ABSENT;
        }

        switch (current) {
            case PRESENT -> {
                record.setReason(null);
                return AttendanceStatus.PRESENT;
            }
            case REGISTERED -> {
                record.setReason("Student only registered for the event");
                return AttendanceStatus.REGISTERED;
            }
            case IDLE -> {
                record.setReason("Student was registered but idle/outside for too long");
                return AttendanceStatus.ABSENT;
            }
            case ABSENT -> {
                record.setReason("Student did not register for the event at all");
                return AttendanceStatus.ABSENT;
            }
            default -> {
                return current;
            }
        }
    }




    private List<Students> getExpectedStudentsForEvent(EventSessions event) {
        //TODO:eligible students
        return studentRepository.findAll();
    }
}


