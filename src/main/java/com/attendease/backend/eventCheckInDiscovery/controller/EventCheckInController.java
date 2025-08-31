package com.attendease.backend.eventCheckInDiscovery.controller;

import com.attendease.backend.eventCheckInDiscovery.service.EventCheckInService;
import com.attendease.backend.eventCheckInDiscovery.service.EventCheckInServiceInterface;
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
public class EventCheckInController {
    private final EventService eventService;
    private final EventCheckInServiceInterface checkInServiceInterface;

    public EventCheckInController(EventService eventService, EventCheckInServiceInterface checkInServiceInterface) {
        this.eventService = eventService;
        this.checkInServiceInterface = checkInServiceInterface;
    }

    // Get all ongoing events
    /**
     * Log the student check-in after verifying eligibility and geofence.
     * 
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
     * 
     * sample body:
     * 
     * {
        "eventId": "{eventId}",
        "studentNumber": "CT00-0000",
        "checkInTime": "2025-08-31T10:00:00",
        "locationId": "{locationId}",
        "latitude": 14.1498,
        "longitude": 120.9555
        }

     *  POST /checkin/{studentNumber}/checkedin
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
