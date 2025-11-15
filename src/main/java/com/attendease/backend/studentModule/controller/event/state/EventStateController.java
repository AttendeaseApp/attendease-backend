package com.attendease.backend.studentModule.controller.event.state;

import com.attendease.backend.domain.events.Registration.Response.EventStartStatusResponse;
import com.attendease.backend.studentModule.service.event.state.EventStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class EventStateController {

    private final EventStateService eventStateService;

    @GetMapping("/{id}/start-status")
    public ResponseEntity<EventStartStatusResponse> getEventStartStatus(@PathVariable String id) {
        EventStartStatusResponse status = eventStateService.getEventStartStatus(id);
        return ResponseEntity.ok(status);
    }
}
