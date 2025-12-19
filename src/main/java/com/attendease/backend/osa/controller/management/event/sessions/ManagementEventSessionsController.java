package com.attendease.backend.osa.controller.management.event.sessions;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.events.Session.Management.Request.EventSessionRequest;
import com.attendease.backend.domain.events.Session.Management.Response.EventCreationResponse;
import com.attendease.backend.osa.service.management.event.sessions.ManagementEventSessionsService;
import java.util.Date;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
public class ManagementEventSessionsController {

    private final ManagementEventSessionsService managementEventSessionsService;

    /**
     * Creates a new event session based on the provided request details.
     *
     * <p>This endpoint validates the incoming request, creates an event with initial status {@link EventStatus#UPCOMING},
     * and associates it with a location if specified. It handles date range validations and generates necessary timestamps.</p>
     *
     * <p><strong>Request Body:</strong> {@link EventSessionRequest} containing event details such as title, description,
     * start/end dates, location ID, and capacity.</p>
     *
     * <p><strong>Response:</strong> {@link EventCreationResponse} with the created event details, including generated ID.</p>
     *
     * @param request the validated {@link EventSessionRequest} object
     * @return {@link ResponseEntity} with status 201 (CREATED) and the {@link EventCreationResponse}
     * @throws IllegalArgumentException if date validations fail or location ID is invalid
     * @see ManagementEventSessionsService#createEvent(EventSessionRequest)
     */
    @PostMapping
    public ResponseEntity<EventCreationResponse> createEvent(@Valid @RequestBody EventSessionRequest request) {
        EventCreationResponse response = managementEventSessionsService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a specific event session by its unique identifier.
     *
     * <p>This endpoint fetches the event details for the given ID. It is useful for viewing individual event information.</p>
     *
     * <p><strong>Path Variable:</strong> {@code id} - the unique string ID of the event session.</p>
     *
     * <p><strong>Response:</strong> {@link EventSessions} object if found, or 404 if not.</p>
     *
     * @param eventId the unique ID of the event session
     * @return {@link ResponseEntity} with the {@link EventSessions} or 404 if not found
     * @throws RuntimeException if no event is found with the provided ID
     * @see ManagementEventSessionsService#getEventById(String)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventSessions> getEventById(@PathVariable("id") String eventId) {
        EventSessions event = managementEventSessionsService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * Retrieves all event sessions, ordered by newest first.
     *
     * <p>This endpoint provides a paginated or full list of all events for administrative overview.
     * Consider adding pagination parameters in future iterations for large datasets.</p>
     *
     * <p><strong>Response:</strong> List of {@link EventSessions}.</p>
     *
     * @return {@link ResponseEntity} with the list of all {@link EventSessions}
     * @see ManagementEventSessionsService#getAllEvents()
     */
    @GetMapping
    public ResponseEntity<List<EventSessions>> getAllEvents() {
        List<EventSessions> events = managementEventSessionsService.getAllEvents();
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
     * <p><strong>Response:</strong> List of {@link EventSessions} matching the status.</p>
     *
     * @param status the {@link EventStatus} to filter by
     * @return {@link ResponseEntity} with the filtered list of {@link EventSessions}
     * @see ManagementEventSessionsService#getEventsByStatus(EventStatus)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventSessions>> getEventsByStatus(@PathVariable("status") EventStatus status) {
        List<EventSessions> events = managementEventSessionsService.getEventsByStatus(status);
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves event sessions within a specified date range.
     *
     * <p>This endpoint filters events whose start/end dates fall within the provided range, inclusive.
     * Useful for reporting or scheduling overviews.</p>
     *
     * <p><strong>Query Parameters:</strong>
     * <ul>
     * <li>{@code from} - start date in ISO format (yyyy-MM-dd'T'HH:mm:ss)</li>
     * <li>{@code to} - end date in ISO format (yyyy-MM-dd'T'HH:mm:ss)</li>
     * </ul></p>
     *
     * <p><strong>Response:</strong> List of {@link EventSessions} within the date range.</p>
     *
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return {@link ResponseEntity} with the filtered list of {@link EventSessions}
     * @see ManagementEventSessionsService#getEventsByDateRange(Date, Date)
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<EventSessions>> getEventsByDateRange(
        @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
        @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to
    ) {
        List<EventSessions> events = managementEventSessionsService.getEventsByDateRange(from, to);
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves event sessions filtered by status and within a specified date range.
     *
     * <p>This endpoint combines status and date filtering for precise querying, e.g., upcoming events in the next week.</p>
     *
     * <p><strong>Query Parameters:</strong>
     * <ul>
     * <li>{@code status} - the {@link EventStatus} enum value</li>
     * <li>{@code from} - start date in ISO format (yyyy-MM-dd'T'HH:mm:ss)</li>
     * <li>{@code to} - end date in ISO format (yyyy-MM-dd'T'HH:mm:ss)</li>
     * </ul></p>
     *
     * <p><strong>Response:</strong> List of {@link EventSessions} matching the criteria.</p>
     *
     * @param status the {@link EventStatus} to filter by
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return {@link ResponseEntity} with the filtered list of {@link EventSessions}
     * @see ManagementEventSessionsService#getEventsByStatusAndDateRange(EventStatus, Date, Date)
     */
    @GetMapping("/status-date-range")
    public ResponseEntity<List<EventSessions>> getEventsByStatusAndDateRange(
        @RequestParam("status") EventStatus status,
        @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
        @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to
    ) {
        List<EventSessions> events = managementEventSessionsService.getEventsByStatusAndDateRange(status, from, to);
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
     * <p><strong>Request Body:</strong> {@link EventSessions} object with fields to update (partial updates supported).</p>
     *
     * <p><strong>Response:</strong> Updated {@link EventSessions} object.</p>
     *
     * @param id the unique ID of the event to update
     * @param updateDTO the {@link EventSessions} containing updated fields
     * @return {@link ResponseEntity} with the updated {@link EventSessions}
     * @throws RuntimeException if no event is found or update is prevented due to status constraints
     * @throws IllegalArgumentException if location ID is invalid or status update is attempted
     * @see ManagementEventSessionsService#updateEvent(String, EventSessions)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<EventSessions> updateEvent(@PathVariable("id") String id, @RequestBody EventSessions updateDTO) {
        EventSessions updatedEvent = managementEventSessionsService.updateEvent(id, updateDTO);
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
     * <p><strong>Response:</strong> Updated {@link EventSessions} with cancelled status.</p>
     *
     * @param id the unique ID of the event to cancel
     * @return {@link ResponseEntity} with the updated {@link EventSessions}
     * @throws RuntimeException if no event is found with the provided ID
     * @see ManagementEventSessionsService#cancelEvent(String)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<EventSessions> cancelEvent(@PathVariable("id") String id) {
        EventSessions canceledEvent = managementEventSessionsService.cancelEvent(id);
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
     * @return {@link ResponseEntity} with 204 status on successful deletion
     * @throws RuntimeException if no event is found or deletion is prevented due to data integrity constraints
     * @see ManagementEventSessionsService#deleteEventById(String)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String id) {
        managementEventSessionsService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }
}
