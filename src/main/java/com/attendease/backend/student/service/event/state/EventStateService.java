package com.attendease.backend.student.service.event.state;

import com.attendease.backend.domain.event.state.checking.EventStateCheckingResponse;

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
     * @return {@link EventStateCheckingResponse} containing flags and a status message
     * @throws IllegalStateException if the event with the given ID is not found
     */
    EventStateCheckingResponse getEventStartStatus(String eventId);

    void broadcastEventStateChange(String eventId);
}
