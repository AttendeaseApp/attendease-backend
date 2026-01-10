package com.attendease.backend.osa.controller.event.monitoring;

import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.osa.service.event.monitoring.ManagementEventMonitoringService;
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
@RequestMapping("/api/osa/event/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class ManagementEventMonitoringController {

    private final ManagementEventMonitoringService managementEventMonitoringService;

    /**
     * {@code getAllEventsForMonitoring} is an API endpoint used to retrieve
     * all events with statuses UPCOMING, REGISTRATION, or ONGOING.
     *
     * @return a list of {@link Event} that are in any of the specified statuses
     */
    @GetMapping("/all-events")
    public ResponseEntity<List<Event>> getAllEventsForMonitoring() {
        List<Event> events = managementEventMonitoringService.getEventWithUpcomingRegistrationOngoingStatuses();
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

