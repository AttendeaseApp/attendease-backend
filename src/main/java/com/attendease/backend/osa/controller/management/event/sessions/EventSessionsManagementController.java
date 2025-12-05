package com.attendease.backend.osa.controller.management.event.sessions;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.events.Session.Management.Request.EventSessionRequest;
import com.attendease.backend.domain.events.Session.Management.Response.EventCreationResponse;
import com.attendease.backend.osa.service.management.event.sessions.EventSessionManagementService;
import java.util.Date;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class EventSessionsManagementController {

    private final EventSessionManagementService eventService;

    @PostMapping
    public ResponseEntity<EventCreationResponse> createEvent(@Valid @RequestBody EventSessionRequest request) {
        EventCreationResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventSessions> getEventById(@PathVariable("id") String eventId) {
        EventSessions event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<EventSessions>> getAllEvents() {
        List<EventSessions> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventSessions>> getEventsByStatus(@PathVariable("status") EventStatus status) {
        List<EventSessions> events = eventService.getEventsByStatus(status);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<EventSessions>> getEventsByDateRange(
        @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
        @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to
    ) {
        List<EventSessions> events = eventService.getEventsByDateRange(from, to);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/status-date-range")
    public ResponseEntity<List<EventSessions>> getEventsByStatusAndDateRange(
        @RequestParam("status") EventStatus status,
        @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
        @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to
    ) {
        List<EventSessions> events = eventService.getEventsByStatusAndDateRange(status, from, to);
        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventSessions> updateEvent(@PathVariable("id") String id, @RequestBody EventSessions updateDTO) {
        EventSessions updatedEvent = eventService.updateEvent(id, updateDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<EventSessions> cancelEvent(@PathVariable("id") String id) {
        EventSessions canceledEvent = eventService.cancelEvent(id);
        return ResponseEntity.ok(canceledEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String id) {
        eventService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }
}
