package com.attendease.backend.osaModule.service.management.event.sessions;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * {@link EventSessionManagementService} is a service used for managing event sessions, including creation, retrieval,
 * updates, and deletion of events.
 *
 * <p>Provides methods to handle event lifecycle operations such as creating upcoming events, updating details,
 * canceling events, and querying by status or date ranges. Ensures date validations and location references are properly handled.</p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventSessionManagementService {

    private final LocationRepository locationRepository;
    private final EventSessionsRepository eventSessionRepository;

    /**
     * Creates a new event session with the provided details.
     * Validates the date range and sets the initial status to {@link EventStatus#UPCOMING}.
     * Associates the event with a location if provided.
     *
     * @param eventSession the {@link EventSessions} object containing event details
     * @return the saved {@link EventSessions} with generated ID and timestamps
     * @throws IllegalArgumentException if date validations fail or location ID is invalid
     */
    public EventSessions createEvent(EventSessions eventSession) {
        log.info("Creating new event session: {}", eventSession.getEventName());

        validateDateRange(eventSession.getTimeInRegistrationStartDateTime(), eventSession.getStartDateTime(), eventSession.getEndDateTime());

        eventSession.setEventStatus(EventStatus.UPCOMING);
        eventSession.setCreatedAt(LocalDateTime.now());
        eventSession.setUpdatedAt(LocalDateTime.now());

        if (eventSession.getEventLocationId() != null) {
            EventLocations location = locationRepository.findById(eventSession.getEventLocationId()).orElseThrow(() -> new IllegalArgumentException("Location ID does not exist"));
            eventSession.setEventLocation(location);
        }

        EventSessions savedEvent = eventSessionRepository.save(eventSession);
        log.info("Successfully created event session with ID: {}", savedEvent.getEventId());
        return savedEvent;
    }

    /**
     * Retrieves an event session by its unique identifier.
     *
     * @param id the unique ID of the event session
     * @return the {@link EventSessions} matching the ID
     * @throws RuntimeException if no event is found with the provided ID
     */
    public EventSessions getEventById(String id) {
        log.info("Retrieving event session with ID: {}", id);
        return eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
    }

    /**
     * Retrieves all event sessions.
     *
     * @return a list of all {@link EventSessions}
     */
    public List<EventSessions> getAllEvents() {
        return eventSessionRepository.findAll();
    }

    /**
     * Retrieves event sessions filtered by a specific status.
     *
     * @param status the {@link EventStatus} to filter by
     * @return a list of {@link EventSessions} with the matching status
     */
    public List<EventSessions> getEventsByStatus(EventStatus status) {
        return eventSessionRepository.findByEventStatus(status);
    }

    /**
     * Retrieves event sessions within a specified date range.
     *
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return a list of {@link EventSessions} within the date range
     */
    public List<EventSessions> getEventsByDateRange(Date from, Date to) {
        return eventSessionRepository.findByDateRange(from, to);
    }

    /**
     * Retrieves event sessions filtered by status and within a specified date range.
     *
     * @param status the {@link EventStatus} to filter by
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return a list of {@link EventSessions} matching the status and date range
     */
    public List<EventSessions> getEventsByStatusAndDateRange(EventStatus status, Date from, Date to) {
        return eventSessionRepository.findByStatusAndDateRange(status, from, to);
    }

    /**
     * Deletes an event session by its unique identifier.
     *
     * @param id the unique ID of the event session to delete
     * @throws RuntimeException if no event is found with the provided ID
     */
    public void deleteEventById(String id) {
        if (!eventSessionRepository.existsById(id)) {
            throw new RuntimeException("Event not found with ID: " + id);
        }
        eventSessionRepository.deleteById(id);
        log.info("Deleted event with ID: {}", id);
    }

    /**
     * Updates an existing event session with new details.
     * Validates location if provided and updates timestamps.
     *
     * @param eventId the unique ID of the event to update
     * @param updateEvent the {@link EventSessions} object containing updated fields
     * @return the updated {@link EventSessions}
     * @throws RuntimeException if no event is found with the provided ID
     * @throws IllegalArgumentException if the location ID is invalid
     */
    public EventSessions updateEvent(String eventId, EventSessions updateEvent) {
        EventSessions existingEvent = eventSessionRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        existingEvent.setEventName(updateEvent.getEventName());
        existingEvent.setDescription(updateEvent.getDescription());
        existingEvent.setStartDateTime(updateEvent.getStartDateTime());
        existingEvent.setEndDateTime(updateEvent.getEndDateTime());
        existingEvent.setTimeInRegistrationStartDateTime(updateEvent.getTimeInRegistrationStartDateTime());
        existingEvent.setEventStatus(updateEvent.getEventStatus() != null ? updateEvent.getEventStatus() : existingEvent.getEventStatus());

        if (updateEvent.getEventLocation() != null) {
            EventLocations location = locationRepository
                .findById(updateEvent.getEventLocation().getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location ID does not exist: " + updateEvent.getEventLocation().getLocationId()));
            existingEvent.setEventLocation(location);
        }

        existingEvent.setUpdatedAt(LocalDateTime.now());

        EventSessions updatedEvent = eventSessionRepository.save(existingEvent);
        log.info("Successfully updated event session with ID: {}", eventId);
        return updatedEvent;
    }

    /**
     * Cancels an event session by setting its status to {@link EventStatus#CANCELLED}.
     *
     * @param id the unique ID of the event to cancel
     * @return the updated {@link EventSessions} with cancelled status
     * @throws RuntimeException if no event is found with the provided ID
     */
    public EventSessions cancelEvent(String id) {
        EventSessions existingEvent = eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));

        existingEvent.setEventStatus(EventStatus.CANCELLED);
        existingEvent.setUpdatedAt(LocalDateTime.now());
        return eventSessionRepository.save(existingEvent);
    }

    private void validateDateRange(LocalDateTime timeInDateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (timeInDateTime == null || startDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("All date fields (time in, start, end) must be provided.");
        }

        if (timeInDateTime.isBefore(now)) {
            throw new IllegalArgumentException("Time-in registration cannot be in the past.");
        }
        if (startDateTime.isBefore(now)) {
            throw new IllegalArgumentException("Event start date/time cannot be in the past.");
        }
        if (endDateTime.isBefore(now)) {
            throw new IllegalArgumentException("Event end date/time cannot be in the past.");
        }

        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Event start date/time must be before event end date/time.");
        }

        long durationInMinutes = Duration.between(startDateTime, endDateTime).toMinutes();
        if (durationInMinutes < 30) {
            throw new IllegalArgumentException("Event duration must be at least 30 minutes.");
        }
        if (durationInMinutes > 360) {
            throw new IllegalArgumentException("Event duration must not exceed 6 hours.");
        }
    }
}
