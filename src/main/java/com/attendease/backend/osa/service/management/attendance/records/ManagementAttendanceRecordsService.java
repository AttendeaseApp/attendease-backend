package com.attendease.backend.osa.service.management.attendance.records;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.History.Response.FinalizedAttendanceRecordsResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;

import java.util.List;

/**
 * {@link ManagementAttendanceRecordsService} is a service responsible for managing attendance records of student.
 *
 * <p>Provides methods to retrieve ongoing and finalized events, fetch attendee details, and update attendance statuses
 * with audit logging support.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-11
 */
public interface ManagementAttendanceRecordsService {

    /**
     * {@code getFinalizedEvents} is used to retrieve all finalized event sessions with attendees per attendance status.
     */
    List<FinalizedAttendanceRecordsResponse> getFinalizedEvents();


    /**
     * {@code getAttendeesByEvent} is used to retrieve all attendees for a specific event, including student information, attendance status,
     * and reasons for absence.
     *
     * @param eventId the unique identifier of the event session
     * @return an {@link EventAttendeesResponse} containing the total number of attendees and their details
     */
    EventAttendeesResponse getAttendeesByEvent(String eventId);

    /**
     * {@code getAttendanceRecordsByStudentId} is used to retrieve all attendance records for a specific student.
     *
     * @param studentId the unique identifier of the student
     * @return a list of {@link AttendanceRecords} associated with the student
     */
    List<AttendanceRecords> getAttendanceRecordsByStudentId(String studentId);

    /**
     * {@code updateAttendanceStatus} is used to update the attendance status for a student's record in a specific event.
     * Enforces audit logging by setting updatedByUserId and relying on @LastModifiedDate for updatedAt.
     *
     * @param studentId The ID of the student.
     * @param eventId The ID of the event session.
     * @param status The new attendance status.
     * @param reason Optional reason for the status change (can be null).
     * @param updatedByUserId The ID of the user performing the update (for audit).
     * @return The updated AttendanceRecords entity, or throws RuntimeException if not found.
     */
    AttendanceRecords updateAttendanceStatus(String studentId, String eventId, AttendanceStatus status, String reason, String updatedByUserId);

    /**
     * {@code getAllAttendanceRecords} is used to retrieve all attendance records.
     *
     * @return a list of all {@link AttendanceRecords}
     */
    List<AttendanceRecords> getAllAttendanceRecords();

    /**
     * {@code deleteAttendanceRecordById} is used to delete an attendance record by its unique identifier.
     *
     * @param recordId the unique ID of the attendance record to delete
     * @throws RuntimeException if the record is not found
     */
    void deleteAttendanceRecordById(String recordId);

    /**
     * {@code deleteAllAttendanceRecords} is used to delete all attendance records.
     * <p><strong>Warning:</strong> This operation is irreversible and will remove all attendance data.</p>
     */
    void deleteAllAttendanceRecords();
}
