/**
 * REST Controller for Event Check-In operations.
 * <p>
 * Endpoints for checking in for mobile app
 */
package com.attendease.backend.attendanceTrackingService.controller;

import com.attendease.backend.attendanceTrackingService.dto.CheckInResponse;
import com.attendease.backend.attendanceTrackingService.service.AttendanceTrackingServiceInterface;
import com.attendease.backend.eventMonitoring.dto.EventCheckInDto;
import com.attendease.backend.eventMonitoring.dto.EventSessionsDto;
import com.attendease.backend.eventMonitoring.service.EventService;
import com.attendease.backend.model.events.EventSessions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/checkin")
@CrossOrigin(origins = "*")
public class AttendanceTrackingController {
    private final EventService eventService;
    private final AttendanceTrackingServiceInterface attendanceTrackingServiceInterface;

    public AttendanceTrackingController(EventService eventService, AttendanceTrackingServiceInterface attendanceTrackingServiceInterface) {
        this.eventService = eventService;
        this.attendanceTrackingServiceInterface = attendanceTrackingServiceInterface;
    }

    /**
     * Fetch all ACTIVE(UPCOMING) and ONGOING events for student to attend.
     * This will return a list of available events including its details.
     * <p>
     * sample return body:
     * {
     * "eventId": "{eventId}",
     * "eventName": "Event Title",
     * "eventLocationId": "{locationId}",
     * "eventStatus": "ACTIVE",
     * "locationId": "locationId",
     * "startDate": ,
     * "endDate": ,
     * "createdAt": ,
     * "updatedAt":
     * }
     * <p>
     * <p>
     * Endpoint:
     * GET /checkin/events/ongoing
     */
    @GetMapping("/events/ongoing")
    public ResponseEntity<List<EventSessionsDto>> getOngoingEvents() {
        List<EventSessions> ongoingEvents = eventService.getOngoingEvents();
        List<EventSessionsDto> dtos = ongoingEvents.stream().map(EventSessionsDto::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Used to checking-in student's attendance.
     * Logs student's attendance as CHECKED_IN for confirmation on registering for the event.
     * Automatically handles checking in and out and the marking of the attendance of the student.
     *
     * <p>
     * sample body:
     * <p>
     * {
     * "eventId": "{eventId}",
     * "studentNumber": "CT00-0000",
     * "checkInTime": "2025-08-31T10:00:00",
     * "locationId": "{locationId}",
     * "latitude": 14.1498,
     * "longitude": 120.9555
     * }
     * <p>
     * <p>
     * Endpoint:
     * POST /checkin/{studentNumber}/checkedin
     */
    @PostMapping("/{studentNumber}/checkedin")
    public ResponseEntity<?> checkInStudent(@PathVariable String studentNumber, @RequestBody EventCheckInDto checkInDTO) {
        if (checkInDTO.getEventId() == null || checkInDTO.getLocationId() == null || checkInDTO.getLatitude() == null || checkInDTO.getLongitude() == null) {
            return ResponseEntity.badRequest().body(new CheckInResponse(false, "Missing required check-in fields"));
        }

        try {
            EventCheckInDto checkedIn = attendanceTrackingServiceInterface.checkInStudent(studentNumber, checkInDTO);
            return ResponseEntity.ok(new CheckInResponse(true, "Check-in successful", checkedIn));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new CheckInResponse(false, e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body(new CheckInResponse(false, "Internal server error processing check-in"));
        }
    }
}
