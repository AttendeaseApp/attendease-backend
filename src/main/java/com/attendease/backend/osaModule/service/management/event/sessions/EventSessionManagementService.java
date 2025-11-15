package com.attendease.backend.osaModule.service.management.event.sessions;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSessionManagementService {

    private final LocationRepository locationRepository;
    private final EventSessionsRepository eventSessionRepository;

    /**
     * Create event
     *
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
     * Get event by ID
     *
     */
    public EventSessions getEventById(String id) {
        log.info("Retrieving event session with ID: {}", id);
        return eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
    }

    /**
     * Get all events
     *
     */
    public List<EventSessions> getAllEvents() {
        return eventSessionRepository.findAll();
    }

    /**
     * Get events by status
     *
     */
    public List<EventSessions> getEventsByStatus(EventStatus status) {
        return eventSessionRepository.findByEventStatus(status);
    }

    /**
     * Get events by date range
     *
     */
    public List<EventSessions> getEventsByDateRange(Date from, Date to) {
        return eventSessionRepository.findByDateRange(from, to);
    }

    /**
     * Get events by status and date range
     *
     */
    public List<EventSessions> getEventsByStatusAndDateRange(EventStatus status, Date from, Date to) {
        return eventSessionRepository.findByStatusAndDateRange(status, from, to);
    }

    /**
     * Delete event by ID
     *
     */
    public void deleteEventById(String id) {
        if (!eventSessionRepository.existsById(id)) {
            throw new RuntimeException("Event not found with ID: " + id);
        }
        eventSessionRepository.deleteById(id);
        log.info("Deleted event with ID: {}", id);
    }

    /**
     * Update Event
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
     * Cancel event
     *
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
    }
}
