package com.attendease.backend.osaModule.service.management.attendance.records;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.Response.AttendeesResponse;
import com.attendease.backend.domain.records.Response.EventAttendeesResponse;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventAttendanceRecordsManagementService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    public List<EventSessions> getOngoingEvents() {
        return eventSessionsRepository.findByEventStatusIn(List.of(EventStatus.ONGOING));
    }

    public List<EventSessions> getFinalizedEvents() {
        return eventSessionsRepository.findByEventStatusIn(List.of(EventStatus.FINALIZED));
    }

    public List<EventSessions> getAllSortedByCreatedAt() {
        return eventSessionsRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<EventSessions> findById(String id) {
        return eventSessionsRepository.findById(id);
    }

    public EventAttendeesResponse getAttendeesByEvent(String eventId) {
        List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(eventId);

        List<AttendeesResponse> attendees = records.stream()
                .filter(Objects::nonNull)
                .filter(record -> record.getStudent() != null && record.getStudent().getUser() != null)
                .map(record -> {
                    var student = record.getStudent();
                    var user = student.getUser();

                    return AttendeesResponse.builder()
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
                            .attendanceRecordId(record.getRecordId())
                            .build();
                })
                .distinct()
                .toList();

        return EventAttendeesResponse.builder()
                .totalAttendees(attendees.size())
                .attendees(attendees)
                .build();
    }

    public List<AttendanceRecords> getAttendanceRecordsByStudentId(String studentId) {
        return attendanceRecordsRepository.findByStudentId(studentId);
    }
}

