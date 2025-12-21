package com.attendease.backend.student.controller.event.state;

import com.attendease.backend.domain.events.Registration.Response.EventStartStatusResponse;
import com.attendease.backend.student.service.event.state.EventStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EventStateController {

    private final EventStateService eventStateService;

    @MessageMapping("/observe-event-state/{id}")
    @SendTo("/topic/read-event-state")
    public EventStartStatusResponse getEventStartStatus(@DestinationVariable String id) {
        return eventStateService.getEventStartStatus(id);
    }
}
