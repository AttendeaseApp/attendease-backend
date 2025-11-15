package com.attendease.backend.studentModule.service.event.retrieval;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for retrieving event session data for the student module.
 * <p>
 * Provides methods for:
 * <ul>
 *     <li>Retrieving upcoming, ongoing, and open-for-registration events</li>
 *     <li>Fetching specific event details by ID</li>
 * </ul>
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventRetrievalService {

    private final EventSessionsRepository eventSessionRepository;

    /**
     * Retrieves all events that are either:
     * <ul>
     *     <li>ONGOING</li>
     *     <li>UPCOMING</li>
     *     <li>REGISTRATION</li>
     * </ul>
     *
     * <p>This is primarily used to display events that students may interact with.</p>
     *
     * @return a list of event sessions matching any of the allowed statuses
     */
    public List<EventSessions> getOngoingRegistrationAndActiveEvents() {
        return eventSessionRepository.findByEventStatusIn(Arrays.asList(EventStatus.ONGOING, EventStatus.UPCOMING, EventStatus.REGISTRATION));
    }

    /**
     * Retrieves event details by its unique identifier.
     *
     * @param id the ID of the event session
     * @return an {@link Optional} containing the event session if found, otherwise empty
     */
    public Optional<EventSessions> getEventById(String id) {
        return eventSessionRepository.findById(id);
    }
}
