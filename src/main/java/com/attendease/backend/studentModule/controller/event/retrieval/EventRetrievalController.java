package com.attendease.backend.studentModule.controller.event.retrieval;

import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.studentModule.service.event.retrieval.EventRetrievalService;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EventRetrievalController {

    private final EventRetrievalService eventsRetrievalService;

    @MessageMapping("/events/{id}")
    @SendToUser("/queue/events/{id}")
    public EventSessions getEventById(@DestinationVariable String id) {
        return eventsRetrievalService.getEventById(id).orElse(null);
    }
}