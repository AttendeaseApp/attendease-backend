/**
 * REST Controller for Event Check-In operations.
 *
 * <b>Base Path:</b> <code>/checkin</code>
 *
 * <b>Endpoints:</b>
 * <ul>
 *   <li><b>GET /checkin/events/ongoing</b> - List all ongoing events.</li>
 *   <li><b>POST /checkin/{studentNumber}/checkedin</b> - Log student check-in.<br>
 *     <b>Sample Request Body:</b>
 *     <pre>
 *     {
 *       "eventId": "{eventId}",
 *       "studentNumber": "CT00-0000",
 *       "checkInTime": "2025-08-31T10:00:00",
 *       "locationId": "{locationId}",
 *       "latitude": 14.1498,
 *       "longitude": 120.9555
 *     }
 *     </pre>
 *   </li>
 * </ul>
 * <b>Responses:</b> JSON objects with event/session/check-in details or error messages.
 */
package com.attendease.backend.attendanceTrackingService.controller;

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
    private final AttendanceTrackingServiceInterface checkInServiceInterface;

    public AttendanceTrackingController(EventService eventService, AttendanceTrackingServiceInterface checkInServiceInterface) {
        this.eventService = eventService;
        this.checkInServiceInterface = checkInServiceInterface;
    }

    // Get all ongoing events

    /**
     * Log the student check-in after verifying eligibility and geofence.
     * <p>
     * GET /checkin/events/ongoing
     */
    @GetMapping("/events/ongoing")
    public ResponseEntity<List<EventSessionsDto>> getOngoingEvents() {
        List<EventSessions> ongoingEvents = eventService.getOngoingEvents();
        List<EventSessionsDto> dtos = ongoingEvents.stream().map(EventSessionsDto::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Log the student check-in after verifying eligibility and geofence.
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
     * POST /checkin/{studentNumber}/checkedin
     */
    @PostMapping("/{studentNumber}/checkedin")
    public ResponseEntity<?> checkInStudent(@PathVariable String studentNumber, @RequestBody EventCheckInDto checkInDTO) {
        try {
            EventCheckInDto checkedIn = checkInServiceInterface.checkInStudent(studentNumber, checkInDTO);
            return ResponseEntity.ok(checkedIn);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Error processing check-in");
        }
    }
}
