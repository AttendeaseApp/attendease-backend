package com.attendease.backend.osaModule.controller.management.attendance.records;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.Response.EventAttendeesResponse;
import com.attendease.backend.osaModule.service.management.attendance.records.EventAttendanceRecordsManagementService;
import com.attendease.backend.domain.events.EventSessions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing attendance records of students for finalized or ongoing events.
 * <p>
 * Accessible only by users with the OSA role.
 */
@RestController
@RequestMapping("/api/attendance/records")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class EventAttendanceRecordsManagementController {

    private final EventAttendanceRecordsManagementService attendanceService;

    /**
     * Retrieves all events sorted by creation date, regardless of their status.
     *
     * @return a list of {@link EventSessions}
     */
    @GetMapping("/event/all")
    public List<EventSessions> getAllEvents() {
        return attendanceService.getAllSortedByCreatedAt();
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
}

