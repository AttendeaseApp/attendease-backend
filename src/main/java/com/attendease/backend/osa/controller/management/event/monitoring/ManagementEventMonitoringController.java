package com.attendease.backend.osa.controller.management.event.monitoring;

import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.osa.service.management.event.monitoring.ManagementEventMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code ManagementEventMonitoringController} is used for monitoring event sessions attendees.
 *
 * <p>This controller provides CRUD operations for event sessions monitoring, ensuring that all endpoints are secured
 * for osa (Office of Student Affairs) role user only.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-25
 */
@RestController
@RequestMapping("/api/events/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('osa')")
public class ManagementEventMonitoringController {

    private final ManagementEventMonitoringService managementEventMonitoringService;

    /**
     * {@code getAllEventsForMonitoring} is an API endpoint used to retrieve
     * all events with statuses UPCOMING, REGISTRATION, or ONGOING.
     *
     * @return a list of {@link EventSessions} that are in any of the specified statuses
     */
    @GetMapping("/all")
    public ResponseEntity<List<EventSessions>> getAllEventsForMonitoring() {
        List<EventSessions> events = managementEventMonitoringService.getEventWithUpcomingRegistrationOngoingStatuses();
        return ResponseEntity.ok(events);
    }

    /**
     * {@code getRegisteredAttendees} is an API endpoint used to retrieve
     * attendees for a specific event who currently have a REGISTERED attendance status.
     * This is used to monitor events and view participants who have registered.
     *
     * @param eventId the ID of the event to retrieve attendees for
     * @return an {@link EventAttendeesResponse} containing total registered attendees and their details
     */
    @GetMapping("/attendees/registered/{eventId}")
    public ResponseEntity<EventAttendeesResponse> getRegisteredAttendees(@PathVariable String eventId) {
        EventAttendeesResponse response = managementEventMonitoringService.getAttendeesByEventWithRegisteredAttendanceStatus(eventId);
        return ResponseEntity.ok(response);
    }
}

