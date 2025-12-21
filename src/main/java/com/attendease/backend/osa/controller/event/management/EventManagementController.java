package com.attendease.backend.osa.controller.event.management;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.event.management.EventManagementRequest;
import com.attendease.backend.domain.event.management.EventManagementResponse;
import com.attendease.backend.osa.service.event.management.EventManagementService;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * {@code ManagementEventSessionsController} is used for managing event sessions.
 *
 * <p>This controller provides CRUD operations for managing event sessions, ensuring that all endpoints are secured
 * for osa (Office of Student Affairs) role user only.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class EventManagementController {

    private final EventManagementService eventManagementService;

    /**
     * Creates a new event session based on the provided request details.
     *
     * <p>This endpoint validates the incoming request, creates an event with initial status {@link EventStatus#UPCOMING},
     * and associates it with a location if specified. It handles date range validations and generates necessary timestamps.</p>
     *
     * <p><strong>Request Body:</strong> {@link EventManagementRequest} containing event details such as title, description,
     * start/end dates, location ID, and capacity.</p>
     *
     * <p><strong>Response:</strong> {@link EventManagementResponse} with the created event details, including generated ID.</p>
     *
     * @param request the validated {@link EventManagementRequest} object
     */
    @PostMapping
    public ResponseEntity<EventManagementResponse> createEvent(@Valid @RequestBody EventManagementRequest request) {
        EventManagementResponse response = eventManagementService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a specific event session by its unique identifier.
     *
     * <p>This endpoint fetches the event details for the given ID. It is useful for viewing individual event information.</p>
     *
     * <p><strong>Path Variable:</strong> {@code id} - the unique string ID of the event session.</p>
     *
     * <p><strong>Response:</strong> {@link Event} object if found, or 404 if not.</p>
     *
     * @param eventId the unique ID of the event session
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable("id") String eventId) {
        Event event = eventManagementService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * Retrieves all event sessions, ordered by newest first.
     *
     * <p>This endpoint provides a paginated or full list of all events for administrative overview.
     * Consider adding pagination parameters in future iterations for large datasets.</p>
     *
     */
    @GetMapping
    public ResponseEntity<List<EventManagementResponse>> getAllEvents() {
        List<EventManagementResponse> events = eventManagementService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves event sessions filtered by a specific status.
     *
     * <p>This endpoint allows filtering events by their current status, such as UPCOMING, ONGOING, or CANCELLED,
     * for targeted management tasks.</p>
     *
     * <p><strong>Path Variable:</strong> {@code status} - the {@link EventStatus} enum value (e.g., UPCOMING).</p>
     *
     * <p><strong>Response:</strong> List of {@link Event} matching the status.</p>
     *
     * @param status the {@link EventStatus} to filter by
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventManagementResponse>> getEventsByStatus(@PathVariable("status") EventStatus status) {
        List<EventManagementResponse> events = eventManagementService.getEventsByStatus(status);
        return ResponseEntity.ok(events);
    }

    /**
     * Updates an existing event session with partial or full details.
     *
     * <p>This endpoint allows modifying event attributes like title, dates, or capacity. It validates changes,
     * updates timestamps, and enforces status-based restrictions (e.g., no updates for CONCLUDED events).
     * Event status cannot be changed here; use dedicated endpoints like cancel.</p>
     *
     * <p><strong>Path Variable:</strong> {@code id} - the unique string ID of the event.</p>
     *
     * <p><strong>Request Body:</strong> {@link Event} object with fields to update (partial updates supported).</p>
     *
     * <p><strong>Response:</strong> Updated {@link Event} object.</p>
     *
     * @param id the unique ID of the event to update
     * @param updateRequest the {@link Event} containing updated fields
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable("id") String id, @RequestBody EventManagementRequest updateRequest) {
        EventManagementResponse updatedEvent = eventManagementService.updateEvent(id, updateRequest);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Cancels an event session by updating its status to {@link EventStatus#CANCELLED}.
     *
     * <p>This endpoint is for administrative cancellation of events, typically for UPCOMING or REGISTRATION status events.
     * It does not delete the event but marks it as cancelled for record-keeping.</p>
     *
     * <p><strong>Path Variable:</strong> {@code id} - the unique string ID of the event.</p>
     *
     * <p><strong>Response:</strong> Updated {@link Event} with cancelled status.</p>
     *
     * @param id the unique ID of the event to cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelEvent(@PathVariable("id") String eventId) {
        Event canceledEvent = eventManagementService.cancelEvent(eventId);
        return ResponseEntity.ok(canceledEvent);
    }

    /**
     * Deletes an event session by its unique identifier.
     *
     * <p>This endpoint permanently removes the event, subject to data integrity checks. Deletion is allowed for
     * UPCOMING or CANCELLED events. For other statuses, it checks for existing attendance records and prevents
     * deletion if any exist to preserve historical data.</p>
     *
     * <p><strong>Path Variable:</strong> {@code id} - the unique string ID of the event.</p>
     *
     * <p><strong>Response:</strong> 204 No Content on success.</p>
     *
     * @param id the unique ID of the event to delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String id) {
        eventManagementService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }
}
