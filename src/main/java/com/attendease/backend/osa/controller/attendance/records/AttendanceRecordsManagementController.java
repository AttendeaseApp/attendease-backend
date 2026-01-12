package com.attendease.backend.osa.controller.attendance.records;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.History.Response.FinalizedAttendanceRecordsResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Request.UpdateAttendanceRequest;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import java.util.List;

import com.attendease.backend.domain.attendance.sorted.SortedAttendanceRecordsResponse;
import com.attendease.backend.domain.enums.attendance.AttendanceSortCriteria;
import com.attendease.backend.osa.service.attendance.records.AttendanceRecordsManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * {@code ManagementAttendanceRecordsController} is used for managing attendance records of student.
 *
 * <p>This controller provides CRUD operations for attendance records management, ensuring that all endpoints are secured
 * for osa (Office of Student Affairs) role user only.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-11
 */
@RestController
@RequestMapping("/api/osa/attendance-records/management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class AttendanceRecordsManagementController {

    private final AttendanceRecordsManagementService attendanceRecordsManagementService;

    /**
     * Retrieves all attendance records.
     *
     * @return a list of all {@link AttendanceRecords}
     */
    @GetMapping("/all")
    public ResponseEntity<List<AttendanceRecords>> getAllAttendanceRecords() {
        List<AttendanceRecords> records = attendanceRecordsManagementService.getAllAttendanceRecords();
        return ResponseEntity.ok(records);
    }
    /**
     * Retrieves all events with EventStatus.FINALIZED.
     *
     * @return a list of {@link FinalizedAttendanceRecordsResponse}
     */
    @GetMapping("/finalized/summary")
    public List<FinalizedAttendanceRecordsResponse> getAllEventsWithFinalizedStatus() {
        return attendanceRecordsManagementService.getFinalizedEvents();
    }

    /**
     * Retrieves all attendees for a specific event, including student information,
     * attendance status, and reasons for absence or tardiness.
     *
     * @param eventId the ID of the event
     * @return an {@link EventAttendeesResponse} containing attendee details and the total number of attendees
     */
    @GetMapping("/event/{eventId}/attendees")
    public EventAttendeesResponse getAttendeesByEvent(@PathVariable String eventId) {
        return attendanceRecordsManagementService.getAttendeesByEvent(eventId);
    }

    /**
     * Retrieves all attendance records for a specific student.
     *
     * @param studentId the ID of the student
     * @return a {@link ResponseEntity} containing a list of {@link AttendanceRecords}
     */
    @GetMapping("/student/{studentId}/records")
    public ResponseEntity<List<AttendanceRecords>> getRecordsByStudentId(@PathVariable String studentId) {
        List<AttendanceRecords> records = attendanceRecordsManagementService.getAttendanceRecordsByStudentId(studentId);
        return ResponseEntity.ok(records);
    }

    /**
     * Retrieves attendance records for a specific event, sorted by academic hierarchy.
     * samples:
     * - GET /api/osa/attendance-records/management/event/{eventId}/sorted?sortBy=CLUSTER
     * - GET /api/osa/attendance-records/management/event/{eventId}/sorted?sortBy=COURSE
     * - GET /api/osa/attendance-records/management/event/{eventId}/sorted?sortBy=SECTION
     * - GET /api/osa/attendance-records/management/event/{eventId}/sorted?sortBy=YEAR_LEVEL
     */
    @GetMapping("/event/{eventId}/sorted")
    public ResponseEntity<SortedAttendanceRecordsResponse> getSortedAttendanceRecords(@PathVariable String eventId, @RequestParam(defaultValue = "SECTION") String sortBy) {
        try {
            AttendanceSortCriteria criteria = AttendanceSortCriteria.valueOf(sortBy.toUpperCase());
            SortedAttendanceRecordsResponse response = attendanceRecordsManagementService.getSortedAttendanceRecords(eventId, criteria);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates the attendance status for a student's record in a specific event.
     *
     * @param studentId the ID of the student
     * @param eventId the ID of the event session
     * @param request the update request containing status and reason.
     * @return a {@link ResponseEntity} containing the updated {@link AttendanceRecords}
     */
    @PutMapping("/{studentId}/event/{eventId}/update-status")
    public ResponseEntity<AttendanceRecords> updateAttendanceStatus(@PathVariable String studentId, @PathVariable String eventId, @RequestBody UpdateAttendanceRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String updatedByUserId = auth.getName();
        AttendanceRecords updatedRecord = attendanceRecordsManagementService.updateAttendanceStatus(studentId, eventId, request.getStatus(), request.getReason(), updatedByUserId);
        return ResponseEntity.ok(updatedRecord);
    }

    /**
     * Deletes an attendance record by its unique identifier.
     *
     * @param recordId the unique ID of the attendance record to delete
     * @return a {@link ResponseEntity} with no content if successful
     */
    @DeleteMapping("/{recordId}/delete")
    public ResponseEntity<Void> deleteAttendanceRecordById(@PathVariable String recordId) {
        attendanceRecordsManagementService.deleteAttendanceRecordById(recordId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all attendance records.
     * <p><strong>Warning:</strong> This operation is irreversible and will remove all attendance data.</p>
     *
     * @return a {@link ResponseEntity} with no content if successful
     */
    @DeleteMapping("/delete/all")
    public ResponseEntity<Void> deleteAllAttendanceRecords() {
        attendanceRecordsManagementService.deleteAllAttendanceRecords();
        return ResponseEntity.noContent().build();
    }
}
