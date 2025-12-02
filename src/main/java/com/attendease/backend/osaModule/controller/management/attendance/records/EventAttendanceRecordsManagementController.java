package com.attendease.backend.osaModule.controller.management.attendance.records;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.History.Response.FinalizedAttendanceRecordsResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Request.UpdateAttendanceRequest;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.osaModule.service.management.attendance.records.EventAttendanceRecordsManagementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing attendance records of students for finalized or ongoing events.
 * <p>
 * Accessible only by users with the OSA role.
 * <p>
 *
 * Authored: jakematthewviado204@gmail.com
 */
@RestController
@RequestMapping("/api/attendance/records")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class EventAttendanceRecordsManagementController {

    private final EventAttendanceRecordsManagementService attendanceService;

    /**
     * Retrieves all events with EventStatus.FINALIZED.
     *
     * @return a list of {@link FinalizedAttendanceRecordsResponse}
     */
    @GetMapping("/event/finalized")
    public List<FinalizedAttendanceRecordsResponse> getAllEventsWithFinalizedStatus() {
        return attendanceService.getFinalizedEvents();
    }

    /**
     * Retrieves all attendees for a specific event, including student information,
     * attendance status, and reasons for absence or tardiness.
     *
     * @param eventId the ID of the event
     * @return an {@link EventAttendeesResponse} containing attendee details and the total number of attendees
     */
    @GetMapping("/attendees/event/{eventId}")
    public EventAttendeesResponse getAttendeesByEvent(@PathVariable String eventId) {
        return attendanceService.getAttendeesByEvent(eventId);
    }

    /**
     * Retrieves a specific event by its unique ID.
     *
     * @param id the ID of the event
     * @return the {@link EventSessions} corresponding to the given ID
     * @throws RuntimeException if no event is found with the provided ID
     */
    @GetMapping("/event/{id}")
    public EventSessions getEventById(@PathVariable String id) {
        return attendanceService.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    /**
     * Retrieves all attendance records for a specific student.
     *
     * @param studentId the ID of the student
     * @return a {@link ResponseEntity} containing a list of {@link AttendanceRecords}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceRecords>> getRecordsByStudentId(@PathVariable String studentId) {
        List<AttendanceRecords> records = attendanceService.getAttendanceRecordsByStudentId(studentId);
        return ResponseEntity.ok(records);
    }

    /**
     * Updates the attendance status for a student's record in a specific event.
     * Uses Spring Security's Authentication to identify the updating user for audit logging.
     *
     * @param studentId the ID of the student
     * @param eventId the ID of the event session
     * @param request the update request containing status and reason.
     * @return a {@link ResponseEntity} containing the updated {@link AttendanceRecords}
     */
    @PutMapping("/{studentId}/event/{eventId}")
    public ResponseEntity<AttendanceRecords> updateAttendanceStatus(@PathVariable String studentId, @PathVariable String eventId, @RequestBody UpdateAttendanceRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String updatedByUserId = auth.getName();

        AttendanceRecords updatedRecord = attendanceService.updateAttendanceStatus(studentId, eventId, request.getStatus(), request.getReason(), updatedByUserId);

        return ResponseEntity.ok(updatedRecord);
    }

    /**
     * Retrieves all attendance records.
     *
     * @return a list of all {@link AttendanceRecords}
     */
    @GetMapping
    public ResponseEntity<List<AttendanceRecords>> getAllAttendanceRecords() {
        List<AttendanceRecords> records = attendanceService.getAllAttendanceRecords();
        return ResponseEntity.ok(records);
    }

    /**
     * Deletes an attendance record by its unique identifier.
     *
     * @param recordId the unique ID of the attendance record to delete
     * @return a {@link ResponseEntity} with no content if successful
     * @throws RuntimeException if the record is not found
     */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteAttendanceRecordById(@PathVariable String recordId) {
        attendanceService.deleteAttendanceRecordById(recordId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all attendance records.
     * <p><strong>Warning:</strong> This operation is irreversible and will remove all attendance data.</p>
     *
     * @return a {@link ResponseEntity} with no content if successful
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllAttendanceRecords() {
        attendanceService.deleteAllAttendanceRecords();
        return ResponseEntity.noContent().build();
    }
}
