package com.attendease.backend.student.controller.event.retrieval;

import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.student.service.event.retrieval.EventRetrievalService;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventRetrievalController {

    private final EventRetrievalService eventRetrievalService;

    @MessageMapping("/events/{id}")
    @SendToUser("/queue/events/{id}")
    public EventSessions getEventById(@DestinationVariable String id) {
        return eventRetrievalService.getEventById(id).orElse(null);
    }

    /**
     * WebSocket endpoint:
     * Client sends:     /app/homepage-events
     * Server responds:  /topic/homepage-events
     */
    @MessageMapping("/homepage-events")
    @SendTo("/topic/homepage-events")
    public List<EventSessions> sendHomepageEvents() {
        return eventRetrievalService.getOngoingRegistrationAndActiveEvents();
    }
}