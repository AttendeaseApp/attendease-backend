package com.attendease.backend.osa.service.management.event.monitoring.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response.AttendeesResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.osa.service.management.event.monitoring.ManagementEventMonitoringService;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagementEventMonitoringServiceImpl implements ManagementEventMonitoringService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    @Override
    public List<EventSessions> getEventWithUpcomingRegistrationOngoingStatuses() {
        return eventSessionsRepository.findByEventStatusIn(List.of(EventStatus.UPCOMING, EventStatus.REGISTRATION, EventStatus.ONGOING));
    }

    @Override
    public EventAttendeesResponse getAttendeesByEventWithRegisteredAttendanceStatus(String eventId) {
        List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(eventId);

        List<AttendeesResponse> attendees = records
            .stream()
            .filter(Objects::nonNull)
            .filter(record -> record.getAttendanceStatus() == AttendanceStatus.REGISTERED || record.getAttendanceStatus() == AttendanceStatus.LATE)
            .filter(record -> record.getStudent() != null && record.getStudent().getUser() != null)
            .map(this::mapToAttendeeResponse)
            .distinct()
            .toList();

        return EventAttendeesResponse.builder().totalAttendees(attendees.size()).attendees(attendees).build();
    }

    private AttendeesResponse mapToAttendeeResponse(AttendanceRecords record) {
        var student = record.getStudent();
        var user = student.getUser();
        String sectionName = (student.getSection() != null) ? student.getSection().getSectionName() : "";

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
            .sectionName(sectionName)
            .attendanceStatus(record.getAttendanceStatus())
            .reason(record.getReason())
            .timeIn(record.getTimeIn())
            .attendanceRecordId(record.getRecordId())
            .build();
    }
}
