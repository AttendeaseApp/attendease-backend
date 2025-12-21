package com.attendease.backend.student.service.event.state;

import com.attendease.backend.domain.events.Registration.Response.EventStartStatusResponse;

/**
 * Service responsible for providing current state of events for the student module.
 * <p>
 * Provides methods for:
 * <ul>
 *     <li>Providing state of events</li>
 * </ul>
 * </p>
 */
public interface EventStateService {

    /**
     * Retrieves the start status of a specific event.
     *
     * @param eventId the ID of the event to check
     * @return {@link EventStartStatusResponse} containing flags and a status message
     * @throws IllegalStateException if the event with the given ID is not found
     */
    EventStartStatusResponse getEventStartStatus(String eventId);
}
