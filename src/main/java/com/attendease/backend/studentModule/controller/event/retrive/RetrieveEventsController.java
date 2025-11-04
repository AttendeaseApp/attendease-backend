package com.attendease.backend.studentModule.controller.event.retrive;

import com.attendease.backend.studentModule.service.event.retrieve.EventsRetrievalService;
import com.attendease.backend.domain.events.EventSessions;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RetrieveEventsController {

    private final EventsRetrievalService eventsRetrievalService;

    @MessageMapping("/events")
    @SendTo("/topic/events")
    public List<EventSessions> getEventsByStatus() {
        return eventsRetrievalService.getOngoingRegistrationAndActiveEvents();
    }
}
