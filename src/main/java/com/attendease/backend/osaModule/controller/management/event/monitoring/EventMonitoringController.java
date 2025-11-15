package com.attendease.backend.osaModule.controller.management.event.monitoring;

import com.attendease.backend.domain.attendance.Response.EventAttendeesResponse;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.osaModule.service.management.event.monitoring.EventMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for monitoring events and attendee registration statuses.
 */
@RestController
@RequestMapping("/api/events/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class EventMonitoringController {

    private final EventMonitoringService eventMonitoringService;

    /**
     * Retrieves all events that have UPCOMING, REGISTRATION, or ONGOING statuses.
     *
     * @return a list of {@link EventSessions} for monitoring purposes
     */
    @GetMapping("/all")
    public ResponseEntity<List<EventSessions>> getAllEventsForMonitoring() {
        List<EventSessions> events = eventMonitoringService.getEventWithUpcomingRegistrationOngoingStatuses();
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves all attendees of a specific event whose attendance status is REGISTERED.
     * <p>
     * This endpoint is useful for monitoring events and seeing who has registered but not yet attended.
     *
     * @param eventId the ID of the event
     * @return {@link EventAttendeesResponse} containing total registered attendees and their details
     */
    @GetMapping("/attendees/registered/{eventId}")
    public ResponseEntity<EventAttendeesResponse> getRegisteredAttendees(@PathVariable String eventId) {
        EventAttendeesResponse response = eventMonitoringService.getAttendeesByEventWithRegisteredAttendanceStatus(eventId);
        return ResponseEntity.ok(response);
    }
}

