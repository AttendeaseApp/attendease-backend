package com.attendease.backend.studentModule.controller.event.retrieval;

import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.studentModule.service.event.retrieval.EventRetrievalService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registration/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class EventRetrievalController {

    private final EventRetrievalService eventsRetrievalService;

    @GetMapping
    public List<EventSessions> getEventsByStatus() {
        return eventsRetrievalService.getOngoingRegistrationAndActiveEvents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventSessions> getEventById(@PathVariable String id) {
        return eventsRetrievalService.getEventById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}