package com.attendease.backend.student.service.event.retrieval;

import com.attendease.backend.domain.events.EventSessions;

import java.util.List;
import java.util.Optional;

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
public interface EventRetrievalService {

    /**
     * Retrieves event details by its unique identifier.
     *
     * @param id the ID of the event session
     * @return an {@link Optional} containing the event session if found, otherwise empty
     */
    Optional<EventSessions> getEventById(String id);

    /**
     * Retrieves all events that are either:
     * <ul>
     *     <li>ONGOING</li>
     *     <li>UPCOMING</li>
     *     <li>REGISTRATION</li>
     * </ul>
     *
     * <p>This is primarily used to display events that student may interact with.</p>
     *
     * @return a list of event sessions matching any of the allowed statuses
     */
    List<EventSessions> getOngoingRegistrationAndActiveEvents();
}
