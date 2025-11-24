package com.attendease.backend.osaModule.service.management.event.monitoring;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response.AttendeesResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link EventMonitoringService} a service used for monitoring ongoing event attendees and event statuses.
 *
 * <p>Provides methods to retrieve ongoing events and fetch attendees</p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Service
@RequiredArgsConstructor
public class EventMonitoringService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    /**
     * Retrieves all events with statuses UPCOMING, REGISTRATION, or ONGOING.
     *
     * @return a list of {@link EventSessions} that are in any of the specified statuses
     */
    public List<EventSessions> getEventWithUpcomingRegistrationOngoingStatuses() {
        return eventSessionsRepository.findByEventStatusIn(List.of(EventStatus.UPCOMING, EventStatus.REGISTRATION, EventStatus.ONGOING));
    }

    /**
     * Retrieves attendees for a specific event who currently have a REGISTERED attendance status.
     * <p>
     * This is used to monitor events and view participants who have registered but not yet checked in or finalized attendance.
     *
     * @param eventId the ID of the event to retrieve attendees for
     * @return an {@link EventAttendeesResponse} containing total registered attendees and their details
     */
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

    /**
     * Maps an {@link AttendanceRecords} entity to an {@link AttendeesResponse}.
     *
     * @param record the attendance record entity
     * @return a mapped {@link AttendeesResponse} object
     */
    private AttendeesResponse mapToAttendeeResponse(AttendanceRecords record) {
        var student = record.getStudent();
        var user = student.getUser();
        String sectionName = (student.getSection() != null) ? student.getSection().getName() : "";
        String courseName = (student.getCourse() != null) ? student.getCourse().getCourseName() : "";
        String clusterName = (student.getCluster() != null) ? student.getCluster().getClusterName() : "";

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
            .courseName(courseName)
            .clusterName(clusterName)
            .attendanceStatus(record.getAttendanceStatus())
            .reason(record.getReason())
            .timeIn(record.getTimeIn())
            .attendanceRecordId(record.getRecordId())
            .build();
    }
}
