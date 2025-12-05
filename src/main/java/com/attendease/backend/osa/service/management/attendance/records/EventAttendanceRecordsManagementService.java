package com.attendease.backend.osaModule.service.management.attendance.records;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.History.Response.FinalizedAttendanceRecordsResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response.AttendeesResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * {@link EventAttendanceRecordsManagementService} is a service used for managing attendance records of students.
 *
 * <p>Provides methods to retrieve ongoing and finalized events, fetch attendee details, and update attendance statuses
 * with audit logging support.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-11-11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventAttendanceRecordsManagementService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    /**
     * Retrieves all ongoing event sessions.
     *
     * @return a list of {@link EventSessions} with status {@link EventStatus#ONGOING}
     */
    public List<EventSessions> getOngoingEvents() {
        return eventSessionsRepository.findByEventStatusIn(List.of(EventStatus.ONGOING));
    }

    /**
     * Retrieves all finalized event sessions with attendees per attendance status.
     *
     * @return a list of {@link EventSessions} with status {@link EventStatus#FINALIZED}
     */
    public List<FinalizedAttendanceRecordsResponse> getFinalizedEvents() {
        List<EventSessions> finalizedEvents = eventSessionsRepository.findByEventStatusIn(List.of(EventStatus.FINALIZED));
        List<FinalizedAttendanceRecordsResponse> responses = new ArrayList<>();

        for (EventSessions event : finalizedEvents) {
            List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(event.getEventId());
            long totalPresent = records
                .stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceStatus.PRESENT)
                .count();
            long totalAbsent = records
                .stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceStatus.ABSENT)
                .count();
            long totalIdle = records
                .stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceStatus.IDLE)
                .count();
            long totalLate = records
                .stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceStatus.LATE)
                .count();

            String locationName = event.getEventLocation() != null ? event.getEventLocation().getLocationName() : null;

            FinalizedAttendanceRecordsResponse response = FinalizedAttendanceRecordsResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .locationName(locationName)
                .timeInRegistrationStartDateTime(event.getTimeInRegistrationStartDateTime())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .eventStatus(event.getEventStatus())
                .totalPresent((int) totalPresent)
                .totalAbsent((int) totalAbsent)
                .totalIdle((int) totalIdle)
                .totalLate((int) totalLate)
                .build();
            responses.add(response);
        }

        return responses;
    }

    /**
     * Retrieves all event sessions sorted by creation date in descending order.
     *
     * @return a list of {@link EventSessions} ordered by {@code createdAt} descending
     */
    public List<EventSessions> getAllSortedByCreatedAt() {
        return eventSessionsRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Retrieves an event session by its unique identifier.
     *
     * @param id the unique ID of the event session
     * @return an {@link Optional} containing the {@link EventSessions} if found, otherwise empty
     */
    public Optional<EventSessions> findById(String id) {
        return eventSessionsRepository.findById(id);
    }

    /**
     * Retrieves all attendees for a specific event, including student information, attendance status,
     * and reasons for absence or tardiness.
     *
     * @param eventId the unique identifier of the event session
     * @return an {@link EventAttendeesResponse} containing the total number of attendees and their details
     */
    public EventAttendeesResponse getAttendeesByEvent(String eventId) {
        List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(eventId);
        List<AttendeesResponse> attendees = records
            .stream()
            .filter(Objects::nonNull)
            .filter(record -> record.getStudent() != null && record.getStudent().getUser() != null)
            .map(record -> {
                var student = record.getStudent();
                var user = student.getUser();
                var section = student.getSection();
                String sectionName = (section != null) ? section.getSectionName() : "";
                String courseName = (section != null && section.getCourse() != null) ? section.getCourse().getCourseName() : "";
                String clusterName = (section != null && section.getCourse() != null && section.getCourse().getCluster() != null) ? section.getCourse().getCluster().getClusterName() : "";

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
                    .timeOut(record.getTimeOut())
                    .attendanceRecordId(record.getRecordId())
                    .build();
            })
            .distinct()
            .toList();
        return EventAttendeesResponse.builder().totalAttendees(attendees.size()).attendees(attendees).build();
    }

    /**
     * Retrieves all attendance records for a specific student.
     *
     * @param studentId the unique identifier of the student
     * @return a list of {@link AttendanceRecords} associated with the student
     */
    public List<AttendanceRecords> getAttendanceRecordsByStudentId(String studentId) {
        return attendanceRecordsRepository.findByStudentId(studentId);
    }

    /**
     * Updates the attendance status for a student's record in a specific event.
     * Enforces audit logging by setting updatedByUserId and relying on @LastModifiedDate for updatedAt.
     *
     * @param studentId The ID of the student.
     * @param eventId The ID of the event session.
     * @param status The new attendance status.
     * @param reason Optional reason for the status change (can be null).
     * @param updatedByUserId The ID of the user performing the update (for audit).
     * @return The updated AttendanceRecords entity, or throws RuntimeException if not found.
     */
    public AttendanceRecords updateAttendanceStatus(String studentId, String eventId, AttendanceStatus status, String reason, String updatedByUserId) {
        Optional<AttendanceRecords> optionalRecord = attendanceRecordsRepository.findByStudentIdAndEventEventId(studentId, eventId);
        if (optionalRecord.isEmpty()) {
            throw new RuntimeException("Attendance record not found for student ID: " + studentId + " and event ID: " + eventId);
        }

        AttendanceRecords record = optionalRecord.get();
        record.setAttendanceStatus(status);
        if (reason != null) {
            record.setReason(reason);
        }
        record.setUpdatedByUserId(updatedByUserId);

        return attendanceRecordsRepository.save(record);
    }

    /**
     * Retrieves all attendance records.
     *
     * @return a list of all {@link AttendanceRecords}
     */
    public List<AttendanceRecords> getAllAttendanceRecords() {
        return attendanceRecordsRepository.findAll();
    }

    /**
     * Deletes an attendance record by its unique identifier.
     *
     * @param recordId the unique ID of the attendance record to delete
     * @throws RuntimeException if the record is not found
     */
    public void deleteAttendanceRecordById(String recordId) {
        if (!attendanceRecordsRepository.existsById(recordId)) {
            throw new RuntimeException("Attendance record not found: " + recordId);
        }
        attendanceRecordsRepository.deleteById(recordId);
        log.info("Deleted attendance record: {}", recordId);
    }

    /**
     * Deletes all attendance records.
     * <p><strong>Warning:</strong> This operation is irreversible and will remove all attendance data.</p>
     */
    public void deleteAllAttendanceRecords() {
        long count = attendanceRecordsRepository.count();
        if (count > 0) {
            attendanceRecordsRepository.deleteAll();
            log.warn("Deleted all {} attendance records", count);
        }
    }
}
