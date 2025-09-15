package com.attendease.backend.eventSessionMonitoringService.controller;

import com.attendease.backend.eventSessionMonitoringService.service.EventSessionMonitoringService;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.students.Students;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class EventSessionsMonitoringController {

    private final EventSessionMonitoringService service;

    @GetMapping("/{eventId}/attendance-report")
    public ResponseEntity<?> getAttendanceStatusReport(@PathVariable String eventId) {
        try {
            var report = service.getAttendanceStatusReport(eventId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/attendees/event/{eventId}")
    public List<Students> getAttendeesByEvent(@PathVariable String eventId) {
        return service.getAttendeesByEvent(eventId);
    }

    @GetMapping
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

