package com.attendease.backend.studentModule.controller.event.retrive;

import com.attendease.backend.studentModule.service.event.retrieve.EventsRetrievalService;
import com.attendease.backend.domain.events.EventSessions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registration/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class RetrieveEventsController {

    private final EventsRetrievalService eventsRetrievalService;

    @GetMapping()
    public ResponseEntity<List<EventSessions>> getEventsByStatus() {
        List<EventSessions> events = eventsRetrievalService.getOngoingRegistrationAndActiveEvents();
        return ResponseEntity.ok(events);
    }
}
