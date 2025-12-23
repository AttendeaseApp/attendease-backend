package com.attendease.backend.osa.service.event.management;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.event.management.EventManagementRequest;
import com.attendease.backend.domain.event.management.EventManagementResponse;

import java.util.List;

/**
 * {@link EventManagementService} is a service responsible for managing event sessions, including creation, retrieval,
 * updates, and deletion of events.
 *
 * <p>Provides methods to handle event lifecycle operations such as creating upcoming events, updating details,
 * canceling events, and querying by status or date ranges. Ensures date validations and location references are properly handled.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
public interface EventManagementService {

    /**
     * {@code createEvent} is used to create new event session with the provided details.
     * Validates the date range and sets the initial status to {@link EventStatus#UPCOMING}.
     * Associates the event with a location if provided.
     *
     * @param request the {@link Event} object containing event details
     * @return the saved {@link Event} with generated ID and timestamps
     */
    EventManagementResponse createEvent(EventManagementRequest request);

    /**
     * {@code getEventById} is used to retrieve an event session by its unique identifier.
     *
     * @param id the unique ID of the event session
     * @return the {@link Event} matching the ID
     */
    Event getEventById(String id);

    /**
     * {@code getAllEvents} is used to retrieve all event sessions (ordered by newest first).
     *
     * @return a list of all {@link Event}
     */
    List<EventManagementResponse> getAllEvents();

    /**
     * {@code getEventsByStatus} is used to retrieve event sessions filtered by a specific status.
     *
     * @param status the {@link EventStatus} to filter by
     * @return a list of {@link Event} with the matching status
     */
    List<EventManagementResponse> getEventsByStatus(EventStatus status);

    /**
     * {@code deleteEventById} is used to delete an event session by its unique identifier.
     * Performs data integrity checks: allows deletion for UPCOMING or CANCELLED events unconditionally.
     * For REGISTRATION, ONGOING, CONCLUDED, or FINALIZED events, prevents deletion if attendance records exist (> 0),
     * throwing a status-specific exception message.
     *
     * @param id the unique ID of the event session to delete
     */
    void deleteEventById(String id);

    /**
     * {@code updateEvent} is used to update an existing event session with new details.
     * Validates location if provided and updates timestamps.
     * Prevents updates for events that are CONCLUDED or FINALIZED to maintain attendance records integrity.
     * Event status cannot be manually updated here; use dedicated methods like cancelEvent or rely on the scheduler for time-based changes.
     *
     * @param eventId the unique ID of the event to update
     * @param updateEvent the {@link Event} object containing updated fields
     * @return the updated {@link Event}
     */
    EventManagementResponse updateEvent(String eventId, EventManagementRequest updateEvent);

    /**
     * {@code cancelEvent} is used to cancel an event session by setting its status to {@link EventStatus#CANCELLED}.
     */
    Event cancelEvent(String eventId);
}
