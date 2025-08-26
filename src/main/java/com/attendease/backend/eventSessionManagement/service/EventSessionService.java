package com.attendease.backend.eventSessionManagement.service;

import com.attendease.backend.eventLocationManagement.repository.LocationRepository;
import com.attendease.backend.eventSessionManagement.dto.EventSessionCreateDTO;
import com.attendease.backend.eventSessionManagement.dto.response.EventSessionResponseDTO;
import com.attendease.backend.eventSessionManagement.repository.EventSessionRepository;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.enums.EventStatus;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventSessionService {

    private final EventSessionRepository eventSessionRepository;
    private final Firestore firestore;
    private final LocationRepository locationRepository;

    public EventSessionService(EventSessionRepository eventSessionRepository, Firestore firestore, LocationRepository locationRepository) {
        this.eventSessionRepository = eventSessionRepository;
        this.firestore = firestore;
        this.locationRepository = locationRepository;
    }

    private static final ZoneId PH_ZONE = ZoneId.of("Asia/Manila");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Date parsePhilippineDateTime(String datetimeStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(datetimeStr, FORMATTER);
        ZonedDateTime zonedDateTime = localDateTime.atZone(PH_ZONE);
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * Create new event session
     */
    public EventSessionResponseDTO createEvent(EventSessionCreateDTO createDTO) throws ExecutionException, InterruptedException {
        log.info("Creating new event session: {}", createDTO.getEventName());

        Date startDateTime = parsePhilippineDateTime(createDTO.getStartDateTime());
        Date endDateTime = parsePhilippineDateTime(createDTO.getEndDateTime());

        validateDateRange(startDateTime, endDateTime);

        EventSessions eventSession = new EventSessions();
        eventSession.setEventName(createDTO.getEventName());
        eventSession.setDescription(createDTO.getDescription());
        eventSession.setStartDateTime(startDateTime);
        eventSession.setEndDateTime(endDateTime);
        eventSession.setEventStatus(EventStatus.ACTIVE);

        if (createDTO.getLocationId() != null && !createDTO.getLocationId().isEmpty()) {
            boolean locationExists = locationRepository.existsById(createDTO.getLocationId());
            if (!locationExists) {
                throw new IllegalArgumentException("Location ID does not exist: " + createDTO.getLocationId());
            }
            DocumentReference locationRef = firestore.collection("eventLocations").document(createDTO.getLocationId());
            eventSession.setEventLocationRefId(locationRef);
        }

        String eventId = eventSessionRepository.save(eventSession);
        log.info("Successfully created event session with ID: {}", eventId);
        return convertToResponseDTO(eventSession);
    }

    /**
     * Get event by ID
     */
    public EventSessionResponseDTO getEventById(String eventId) throws ExecutionException, InterruptedException {
        log.info("Retrieving event session with ID: {}", eventId);
        Optional<EventSessions> eventSession = eventSessionRepository.findById(eventId);
        if (eventSession.isEmpty()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
        EventSessionResponseDTO responseDTO = convertToResponseDTO(eventSession.get());
        log.info("Successfully retrieved event session with ID: {}", eventId);
        return responseDTO;
    }

    /**
     * Get all events
     */
    public List<EventSessionResponseDTO> getAllEvents() throws ExecutionException, InterruptedException {
        List<EventSessions> events = eventSessionRepository.findAll();
        return events.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Get all events by status e.g. ACTIVE, CONCLUDED, ONGOING, CANCELLED
     */
    public List<EventSessionResponseDTO> getEventsByStatus(EventStatus status) throws ExecutionException, InterruptedException {
        List<EventSessions> events = eventSessionRepository.findByStatus(status);
        return events.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Get all events with date range parameter
     */
    public List<EventSessionResponseDTO> getEventsByDateRange(Date from, Date to) throws ExecutionException, InterruptedException {
        List<EventSessions> events = eventSessionRepository.findByDateRange(from, to);
        return events.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Get all events with date range and event status parameter
     */
    public List<EventSessionResponseDTO> getEventsByStatusAndDateRange(EventStatus status, Date from, Date to) throws ExecutionException, InterruptedException {
        List<EventSessions> events = eventSessionRepository.findByStatusAndDateRange(status, from, to);
        return events.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Delete event using eventId
     */
    public void deleteEventById(String eventId) throws ExecutionException, InterruptedException {
        if (!eventSessionRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
        eventSessionRepository.deleteById(eventId);
        log.info("Deleted event with ID: {}", eventId);
    }

    /**
     * Update existing event using eventId
     */
    public EventSessionResponseDTO updateEvent(String eventId, EventSessionCreateDTO updateDTO) throws ExecutionException, InterruptedException {
        Optional<EventSessions> existingEventOpt = eventSessionRepository.findById(eventId);
        if (existingEventOpt.isEmpty()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
        EventSessions existingEvent = existingEventOpt.get();

        Date startDateTime = parsePhilippineDateTime(updateDTO.getStartDateTime());
        Date endDateTime = parsePhilippineDateTime(updateDTO.getEndDateTime());

        validateDateRange(startDateTime, endDateTime);

        existingEvent.setEventName(updateDTO.getEventName());
        existingEvent.setDescription(updateDTO.getDescription());
        existingEvent.setStartDateTime(startDateTime);
        existingEvent.setEndDateTime(endDateTime);

        if (updateDTO.getLocationId() != null && !updateDTO.getLocationId().isEmpty()) {
            boolean locationExists = locationRepository.existsById(updateDTO.getLocationId());
            if (!locationExists) {
                throw new IllegalArgumentException("Location ID does not exist: " + updateDTO.getLocationId());
            }
            DocumentReference locationRef = firestore.collection("eventLocations").document(updateDTO.getLocationId());
            existingEvent.setEventLocationRefId(locationRef);
        }

        eventSessionRepository.save(existingEvent);

        return convertToResponseDTO(existingEvent);
    }

    /**
     * Cancel existing event using eventId
     */
    public EventSessionResponseDTO cancelEvent(String eventId) throws ExecutionException, InterruptedException {
        Optional<EventSessions> existingEventOpt = eventSessionRepository.findById(eventId);
        if (existingEventOpt.isEmpty()) {
            throw new RuntimeException("Event not found with ID: " + eventId);
        }
        EventSessions existingEvent = existingEventOpt.get();
        existingEvent.setEventStatus(EventStatus.CANCELLED);
        eventSessionRepository.save(existingEvent);
        return convertToResponseDTO(existingEvent);
    }

    //HELPER METHODSS

    /**
     * Convert EventSessions entity to ResponseDTO
     */
    private EventSessionResponseDTO convertToResponseDTO(EventSessions eventSession) {
        EventSessionResponseDTO dto = new EventSessionResponseDTO();
        dto.setEventId(eventSession.getEventId());
        dto.setEventName(eventSession.getEventName());
        dto.setDescription(eventSession.getDescription());
        dto.setStartDateTime(eventSession.getStartDateTime());
        dto.setEndDateTime(eventSession.getEndDateTime());
        dto.setEventStatus(eventSession.getEventStatus());
        dto.setCreatedAt(eventSession.getCreatedAt());
        dto.setUpdatedAt(eventSession.getUpdatedAt());

        if (eventSession.getCreatedByUserRefId() != null) {
            dto.setCreatedByUserRefId(eventSession.getCreatedByUserRefId().getId());
        }

        return dto;
    }

    /**
     * Validate date range
     */
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
