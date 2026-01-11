package com.attendease.backend.student.service.event.state.impl;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.event.state.checking.EventStateCheckingResponse;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.student.service.event.state.EventStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventStateServiceImpl implements EventStateService {

    private final EventRepository eventRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public EventStateCheckingResponse getEventStartStatus(String eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalStateException("Event not found"));
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

        return new EventStateCheckingResponse(eventId, hasStarted, isOngoing, hasEnded, message);
    }

    @Override
    public void broadcastEventStateChange(String eventId) {
        try {
            EventStateCheckingResponse response = getEventStartStatus(eventId);
            messagingTemplate.convertAndSend("/topic/read-event-state", response);
            log.info("Broadcasted event state change for event: {} - Status: {}", eventId, response.getStatusMessage());
        } catch (Exception e) {
            log.error("Failed to broadcast event state for event: {}", eventId, e);
        }
    }
}
