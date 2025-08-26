package com.attendease.backend.eventSessionManagement.controller;

import com.attendease.backend.eventSessionManagement.dto.*;
import com.attendease.backend.eventSessionManagement.dto.response.EventSessionResponseDTO;
import com.attendease.backend.eventSessionManagement.service.session.EventSessionServiceInterface;
import com.attendease.backend.model.enums.EventStatus;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("v1/api/events")
@CrossOrigin(origins = "*")
@Slf4j
public class EventSessionController {

    private final EventSessionServiceInterface eventSessionService;

    public EventSessionController(EventSessionServiceInterface eventSessionService) {
        this.eventSessionService = eventSessionService;
    }

    //TODO: ELIGIBLE STUDENTS
    /**
     * Create new event session.
     * Sample endpoint:
     * POST v1/api/events/create-event
     */
    @PostMapping("create-event")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventSessionCreateDTO createDTO) {

        try {
            log.info("Creating new event: {}", createDTO.getEventName());

            EventSessionResponseDTO response = eventSessionService.createEvent(createDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating event: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error creating event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create event: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error creating event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    /**
     * Get event session by id.
     * Sample endpoint:
     * GET v1/api/events/{eventId}
     */
    @GetMapping("{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable String eventId) {
        try {
            EventSessionResponseDTO response = eventSessionService.getEventById(eventId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error fetching event with ID {}: {}", eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error fetching event with ID {}: {}", eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    /**
     * Get all event sessions.
     * Sample endpoint:
     * GET v1/api/events/all
     */
    @GetMapping("all")
    public ResponseEntity<?> getAllEvents() {
        try {
            List<EventSessionResponseDTO> events = eventSessionService.getAllEvents();
            return ResponseEntity.ok(events);

        } catch (Exception e) {
            log.error("Error fetching all events: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve events");
        }
    }

    /**
     * Get all event sessions by its status.
     *
     * Sample endpoint on getting ACTIVE events:
     * GET /v1/api/events/by-status?status=ACTIVE
     *
     * Sample endpoint on getting the CONCLUDED events:
     * GET /v1/api/events/by-status?status=CONCLUDED
     */
    @GetMapping("by-status")
    public ResponseEntity<?> getEventsByStatus(@RequestParam EventStatus status) {
        try {
            List<EventSessionResponseDTO> events = eventSessionService.getEventsByStatus(status);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching events by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve events by status");
        }
    }

    /**
     * Get all event sessions by date range
     *
     * Sample endpoint on getting ACTIVE events with date range:
     * GET /v1/api/events/by-date-range?from=2025-08-26T00:00:00&to=2025-08-28T23:59:59
     */
    @GetMapping("by-date-range")
    public ResponseEntity<?> getEventsByDateRange(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        try {
            Date fromDate = parseDate(from);
            Date toDate = parseDate(to);
            List<EventSessionResponseDTO> events = eventSessionService.getEventsByDateRange(fromDate, toDate);
            return ResponseEntity.ok(events);

        } catch (ParseException e) {
            log.error("Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss");

        } catch (Exception e) {
            log.error("Error fetching events by date range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve events by date range");
        }
    }

    /**
     * Get all event sessions by status and date range
     *
     * Sample endpoint on getting active event with date range:
     * GET /v1/api/events/by-status-and-date-range?status=ACTIVE&from=2025-08-26&to=2025-08-28
     */
    @GetMapping("by-status-and-date-range")
    public ResponseEntity<?> getEventsByStatusAndDateRange(@RequestParam EventStatus status, @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        try {
            Date fromDate = parseDate(from);
            Date toDate = parseDate(to);
            List<EventSessionResponseDTO> events = eventSessionService.getEventsByStatusAndDateRange(status, fromDate, toDate);
            return ResponseEntity.ok(events);
        } catch (ParseException e) {
            log.error("Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss");

        } catch (Exception e) {
            log.error("Error fetching events by status and date range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve events by status and date range");
        }
    }

    /**
     * Update event session by id.
     *
     * Sample endpoint on updating event by id:
     * PUT /v1/api/events/{eventId}
     */
    @PutMapping("{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable String eventId, @Valid @RequestBody EventSessionCreateDTO updateDTO) {
        try {
            EventSessionResponseDTO response = eventSessionService.updateEvent(eventId, updateDTO);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating event: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());

        } catch (RuntimeException e) {
            log.error("Error updating event: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error updating event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update event: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error updating event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    /**
     * Set event status to CANCELLED
     *
     * Sample endpoint on updating event by id:
     * PUT /v1/api/events/cancel/{eventId}
     */
    @PutMapping("/cancel/{eventId}")
    public ResponseEntity<?> cancelEvent(@PathVariable String eventId) {
        try {
            EventSessionResponseDTO response = eventSessionService.cancelEvent(eventId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error cancelling event: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());

        } catch (RuntimeException e) {
            log.error("Error cancelling event: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error cancelling event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to cacel event: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error cancelling event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    /**
     * Delete event session by id.
     *
     * Sample endpoint on deleting event by id:
     * DELETE /v1/api/events/{eventId}
     */
    @DeleteMapping("{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable String eventId) {
        try {
            eventSessionService.deleteEventById(eventId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.error("Error deleting event with ID {}: {}", eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error deleting event with ID {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete event: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error deleting event with ID {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    //HELPER METHODS :)
    private Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateStr);
        } catch (ParseException e) {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        }
    }
}
