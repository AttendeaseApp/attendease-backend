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
import java.time.ZoneId;
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

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository.findByEventEventId(eventId);
        List<Students> expectedStudents = getExpectedStudentsForEvent(event);
        Set<String> studentsWithRecords = attendanceRecords.stream().map(record -> record.getStudent().getId()).collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        for (AttendanceRecords record : attendanceRecords) {
            AttendanceStatus currentStatus = record.getAttendanceStatus();
            AttendanceStatus evaluatedStatus = evaluateAttendanceAfterEventEnds(event, record);

            if (evaluatedStatus == null || evaluatedStatus == currentStatus) continue;

            record.setTimeOut(now);
            record.setAttendanceStatus(evaluatedStatus);
            attendanceRecordsRepository.save(record);
            log.info("Finalized attendance for student {} in event {}: {}", record.getStudent().getId(), eventId, evaluatedStatus);
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
                log.info("Marked ABSENT for missing student {} in event {}", student.getId(), eventId);
            }
        }
    }


    private AttendanceStatus evaluateAttendanceAfterEventEnds(EventSessions event, AttendanceRecords record) {
        if (record.getTimeIn() == null) {
            record.setReason("Did not check in for the event: " + event.getEventName());
            return AttendanceStatus.ABSENT;
        }
        LocalDateTime eventStart = event.getStartDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (!record.getTimeIn().isBefore(eventStart)) {
            record.setReason("Late check-in for the event: " + event.getEventName());
            return AttendanceStatus.ABSENT;
        }
        record.setReason(null);
        return AttendanceStatus.PRESENT;
    }



    private List<Students> getExpectedStudentsForEvent(EventSessions event) {
        //TODO:eligible students
        return studentRepository.findAll();
    }
}


