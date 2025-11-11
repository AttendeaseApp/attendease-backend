package com.attendease.backend.osaModule.controller.management.event.monitoring;

import com.attendease.backend.domain.records.Response.EventAttendeesRecordsResponse;
import com.attendease.backend.osaModule.service.management.event.monitoring.EventSessionMonitoringService;
import com.attendease.backend.domain.events.EventSessions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class EventSessionsMonitoringController {

    private final EventSessionMonitoringService service;

    @GetMapping("/attendees/event/{eventId}")
    public List<EventAttendeesRecordsResponse> getAttendeesByEvent(@PathVariable String eventId) {
        return service.getAttendeesByEvent(eventId);
    }

    @GetMapping("/all")
    public List<EventSessions> getAllEvents() {
        return service.getAllSortedByCreatedAt();
    }

    @GetMapping("/{id}")
    public EventSessions getEventById(@PathVariable String id) {
        return service.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @GetMapping("/ended")
    public List<EventSessions> getEndedEvents() {
        return service.getEndedEvents();
    }
}

