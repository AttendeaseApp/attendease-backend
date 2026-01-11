package com.attendease.backend.student.controller.event.retrieval;

import com.attendease.backend.domain.event.Event;
import com.attendease.backend.student.service.event.retrieval.EventRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class EventRetrievalController {

    private final EventRetrievalService eventRetrievalService;

    @MessageMapping("/events/{id}")
    @SendToUser("/queue/events/{id}")
    public Event getEventById(@DestinationVariable String id) {
        log.info("Client requested event: {}", id);
        long startTime = System.currentTimeMillis();
        Event event = eventRetrievalService.getEventById(id).orElse(null);
        long duration = System.currentTimeMillis() - startTime;
        log.info("Retrieved event {} in {}ms", id, duration);
        return event;
    }
}