package com.attendease.backend.automatedAttendanceTrackingService.controller;

import com.attendease.backend.automatedAttendanceTrackingService.service.RetrieveOngoingEventsService;
import com.attendease.backend.domain.events.EventSessions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/checkin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class CheckOngoingEventsController {
    private final RetrieveOngoingEventsService retrieveOngoingEventsService;
    @GetMapping()
    public ResponseEntity<List<EventSessions>> getEventsByStatus() {
        List<EventSessions> events = retrieveOngoingEventsService.getOngoingRegistrationAndActiveEvents();
        return ResponseEntity.ok(events);
    }
}
