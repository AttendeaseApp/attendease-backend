package com.attendease.backend.eventSessionsManagement.service;

import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.enums.EventStatus;
import com.attendease.backend.model.events.Response.EventCreationResponse;
import com.attendease.backend.model.locations.EventLocations;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSessionManagementService {

    private final LocationRepository locationRepository;
    private final EventSessionsRepository eventSessionRepository;

    private static final ZoneId PH_ZONE = ZoneId.of("Asia/Manila");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Date parsePhilippineDateTime(String datetimeStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(datetimeStr, FORMATTER);
        ZonedDateTime zonedDateTime = localDateTime.atZone(PH_ZONE);
        return Date.from(zonedDateTime.toInstant());
    }

    public EventCreationResponse createEvent(EventCreationResponse eventCreationResponse) {
        log.info("Creating new event session: {}", eventCreationResponse.getEventName());

        Date startDateTime = eventCreationResponse.getStartDateTime();
        Date endDateTime = eventCreationResponse.getEndDateTime();

        validateDateRange(startDateTime, endDateTime);

        EventSessions eventSession = new EventSessions();
        eventSession.setEventName(eventCreationResponse.getEventName());
        eventSession.setDescription(eventCreationResponse.getDescription());
        eventSession.setStartDateTime(startDateTime);
        eventSession.setEndDateTime(endDateTime);
        eventSession.setEventStatus(EventStatus.ACTIVE);
        
        if (eventCreationResponse.getEventLocationRefId() != null && !eventCreationResponse.getEventLocationRefId().isEmpty()) {
            Optional<EventLocations> locationOpt = locationRepository.findById(eventCreationResponse.getEventLocationRefId());
            if (locationOpt.isEmpty()) {
                throw new IllegalArgumentException("Location ID does not exist: " + eventCreationResponse.getEventLocationRefId());
            }
            eventSession.setEventLocation(locationOpt.get());
        }

        EventSessions savedEvent = eventSessionRepository.save(eventSession);
        log.info("Successfully created event session with ID: {}", savedEvent.getEventId());
        return convertToResponseDTO(savedEvent);
    }

    public EventCreationResponse getEventById(String eventId) {
        log.info("Retrieving event session with ID: {}", eventId);
        Optional<EventSessions> eventSession = eventSessionRepository.findById(eventId);
        if (eventSession.isEmpty()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
        return convertToResponseDTO(eventSession.get());
    }

    public List<EventCreationResponse> getAllEvents() {
        List<EventSessions> events = eventSessionRepository.findAll();
        return events.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    public List<EventCreationResponse> getEventsByStatus(EventStatus status) {
        List<EventSessions> events = eventSessionRepository.findByEventStatus(status);
        return events.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    public List<EventCreationResponse> getEventsByDateRange(Date from, Date to) {
        List<EventSessions> events = eventSessionRepository.findByDateRange(from, to);
        return events.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    public List<EventCreationResponse> getEventsByStatusAndDateRange(EventStatus status, Date from, Date to) {
        List<EventSessions> events = eventSessionRepository.findByStatusAndDateRange(status, from, to);
        return events.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    public void deleteEventById(String eventId) {
        if (!eventSessionRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
        eventSessionRepository.deleteById(eventId);
        log.info("Deleted event with ID: {}", eventId);
    }

    public EventCreationResponse updateEvent(String eventId, EventCreationResponse updateDTO) {
        Optional<EventSessions> existingEventOpt = eventSessionRepository.findById(eventId);
        if (existingEventOpt.isEmpty()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
        EventSessions existingEvent = existingEventOpt.get();

        // Update fields
        existingEvent.setEventName(updateDTO.getEventName());
        existingEvent.setDescription(updateDTO.getDescription());

        Date startDateTime = parsePhilippineDateTime(String.valueOf(updateDTO.getStartDateTime()));
        Date endDateTime = parsePhilippineDateTime(String.valueOf(updateDTO.getEndDateTime()));

        validateDateRange(startDateTime, endDateTime);

        existingEvent.setStartDateTime(startDateTime);
        existingEvent.setEndDateTime(endDateTime);

        if (updateDTO.getEventLocationRefId() != null && !updateDTO.getEventLocationRefId().isEmpty()) {
            Optional<EventLocations> locationOpt = locationRepository.findById(updateDTO.getEventLocationRefId());
            if (locationOpt.isEmpty()) {
                throw new IllegalArgumentException("Location ID does not exist: " + updateDTO.getEventLocationRefId());
            }
            existingEvent.setEventLocation(locationOpt.get());
        }

        existingEvent.setEventStatus(updateDTO.getEventStatus() != null ? updateDTO.getEventStatus() : existingEvent.getEventStatus());

        EventSessions updatedEvent = eventSessionRepository.save(existingEvent);
        log.info("Successfully updated event session with ID: {}", eventId);
        return convertToResponseDTO(updatedEvent);
    }

    public EventCreationResponse cancelEvent(String eventId) {
        Optional<EventSessions> existingEventOpt = eventSessionRepository.findById(eventId);
        if (existingEventOpt.isEmpty()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
        EventSessions existingEvent = existingEventOpt.get();
        existingEvent.setEventStatus(EventStatus.CANCELLED);
        eventSessionRepository.save(existingEvent);
        return convertToResponseDTO(existingEvent);
    }

    private EventCreationResponse convertToResponseDTO(EventSessions eventSession) {
        EventCreationResponse dto = new EventCreationResponse();
        dto.setEventId(eventSession.getEventId());
        dto.setEventName(eventSession.getEventName());
        dto.setDescription(eventSession.getDescription());
        dto.setStartDateTime(eventSession.getStartDateTime());
        dto.setEndDateTime(eventSession.getEndDateTime());
        dto.setEventStatus(eventSession.getEventStatus());
        dto.setCreatedAt(eventSession.getCreatedAt());
        dto.setUpdatedAt(eventSession.getUpdatedAt());

        if (eventSession.getEventLocation() != null) {
            dto.setEventLocationRefId(eventSession.getEventLocation().getLocationId());
        }
        return dto;
    }

    private void validateDateRange(Date startDateTime, Date endDateTime) {
        if (startDateTime != null && endDateTime != null) {
            if (startDateTime.after(endDateTime)) {
                throw new IllegalArgumentException("Start date time cannot be after end date time");
            }
            Date now = new Date();
            if (startDateTime.before(now)) {
                throw new IllegalArgumentException("Start date time cannot be in the past");
            }
        }
    }
}
