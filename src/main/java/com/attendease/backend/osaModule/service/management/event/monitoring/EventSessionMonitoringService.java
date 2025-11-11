package com.attendease.backend.osaModule.service.management.event.monitoring;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.Response.EventAttendeesRecordsResponse;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EventSessionMonitoringService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final StudentRepository studentRepository;

    public List<EventSessions> getOngoingEvents() {
        return eventSessionsRepository.findByEventStatusIn(Arrays.asList(EventStatus.ONGOING, EventStatus.UPCOMING));
    }

    public List<EventSessions> getEndedEvents() {
        return eventSessionsRepository.findByEndDateTimeBeforeAndEventStatus(new Date(), EventStatus.ONGOING);
    }

    public List<EventSessions> getAllSortedByCreatedAt() {
        return eventSessionsRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<EventSessions> findById(String id) {
        return eventSessionsRepository.findById(id);
    }

    public List<EventAttendeesRecordsResponse> getAttendeesByEvent(String eventId) {
        List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(eventId);

        return records.stream()
                .filter(Objects::nonNull)
                .filter(record -> record.getStudent() != null && record.getStudent().getUser() != null)
                .map(record -> {
                    var student = record.getStudent();
                    var user = student.getUser();

                    return EventAttendeesRecordsResponse.builder()
                            .userId(user.getUserId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .contactNumber(user.getContactNumber())
                            .accountStatus(user.getAccountStatus())
                            .userType(user.getUserType())
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())

                            .studentId(student.getId())
                            .studentNumber(student.getStudentNumber())
                            .section(student.getSectionId())
                            .course(student.getCourseId())

                            .attendanceStatus(record.getAttendanceStatus())
                            .reason(record.getReason())
                            .build();
                })
                .distinct()
                .toList();
    }
}

