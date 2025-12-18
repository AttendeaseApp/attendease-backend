package com.attendease.backend.student.service.event.state;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.events.Registration.Response.EventStartStatusResponse;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for providing current state of events for the student module.
 * <p>
 * Provides methods for:
 * <ul>
 *     <li>Providing state of events</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EventStateService {

    private final EventSessionsRepository eventSessionsRepository;

    /**
     * Retrieves the start status of a specific event.
     *
     * @param eventId the ID of the event to check
     * @return {@link EventStartStatusResponse} containing flags and a status message
     * @throws IllegalStateException if the event with the given ID is not found
     */
    public EventStartStatusResponse getEventStartStatus(String eventId) {
        EventSessions event = eventSessionsRepository.findById(eventId).orElseThrow(() -> new IllegalStateException("Event not found"));
        EventStatus status = event.getEventStatus();

        boolean hasStarted = status == EventStatus.ONGOING;
        boolean isOngoing = status == EventStatus.ONGOING;
        boolean hasEnded = status == EventStatus.CONCLUDED || status == EventStatus.FINALIZED;

        String message = switch (status) {
            case REGISTRATION -> "Event is in registration period.";
            case UPCOMING -> "Event has not started.";
            case ONGOING -> "Event is currently ongoing.";
            case CONCLUDED -> "Event has ended (processing in progress).";
            case CANCELLED -> "Event has been cancelled.";
            case FINALIZED -> "Event fully completed.";
        };

        return new EventStartStatusResponse(eventId, hasStarted, isOngoing, hasEnded, message);
    }
}
