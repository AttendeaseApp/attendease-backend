package com.attendease.backend.student.controller.event.retrieval.homepage;

import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.student.service.event.retrieval.homepage.HomepageEventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomepageEventsController {

    private final HomepageEventsService homepageEventsService;

    /**
     * WebSocket endpoint:
     * Client sends:     /app/homepage-events
     * Server responds:  /topic/homepage-events
     */
    @MessageMapping("/homepage-events")
    @SendTo("/topic/homepage-events")
    public List<EventSessions> sendHomepageEvents() {
        return homepageEventsService.getOngoingRegistrationAndActiveEvents();
    }
}
