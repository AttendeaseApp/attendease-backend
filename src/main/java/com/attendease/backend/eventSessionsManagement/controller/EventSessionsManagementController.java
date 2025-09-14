package com.attendease.backend.eventSessionsManagement.controller;

import com.attendease.backend.eventSessionsManagement.service.EventSessionManagementService;
import com.attendease.backend.model.enums.EventStatus;
import com.attendease.backend.model.events.Response.EventCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventSessionsManagementController {

    private final EventSessionManagementService eventService;

    @PostMapping
    public ResponseEntity<EventCreationResponse> createEvent(@RequestBody EventCreationResponse eventCreationResponse) {
        EventCreationResponse createdEvent = eventService.createEvent(eventCreationResponse);
        return ResponseEntity.ok(createdEvent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventCreationResponse> getEventById(@PathVariable("id") String eventId) {
        EventCreationResponse event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<EventCreationResponse>> getAllEvents() {
        List<EventCreationResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventCreationResponse>> getEventsByStatus(@PathVariable("status") EventStatus status) {
        List<EventCreationResponse> events = eventService.getEventsByStatus(status);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<EventCreationResponse>> getEventsByDateRange(
            @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
            @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to) {
        List<EventCreationResponse> events = eventService.getEventsByDateRange(from, to);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/status-date-range")
    public ResponseEntity<List<EventCreationResponse>> getEventsByStatusAndDateRange(
            @RequestParam("status") EventStatus status,
            @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
            @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to) {
        List<EventCreationResponse> events = eventService.getEventsByStatusAndDateRange(status, from, to);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventCreationResponse> updateEvent(
            @PathVariable("id") String eventId,
            @RequestBody EventCreationResponse updateDTO) {
        EventCreationResponse updatedEvent = eventService.updateEvent(eventId, updateDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<EventCreationResponse> cancelEvent(@PathVariable("id") String eventId) {
        EventCreationResponse canceledEvent = eventService.cancelEvent(eventId);
        return ResponseEntity.ok(canceledEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String eventId) {
        eventService.deleteEventById(eventId);
        return ResponseEntity.noContent().build();
    }
}

