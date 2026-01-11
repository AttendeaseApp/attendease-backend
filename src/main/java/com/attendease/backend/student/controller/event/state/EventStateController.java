package com.attendease.backend.student.controller.event.state;

import com.attendease.backend.domain.event.state.checking.EventStateCheckingResponse;
import com.attendease.backend.student.service.event.state.EventStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class EventStateController {

    private final EventStateService eventStateService;

    // Client subscribes to: /topic/event-state/{eventId}
    @MessageMapping("/observe-event-state/{eventId}")
    @SendTo("/topic/read-event-state")
    public EventStateCheckingResponse getEventState(@DestinationVariable String eventId) {
        log.info("Client requested event state for: {}", eventId);
        return eventStateService.getEventStartStatus(eventId);
    }
}
