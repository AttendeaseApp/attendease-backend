package com.attendease.backend.studentModule.controller.event.checkin;

import com.attendease.backend.domain.records.EventCheckIn.RegistrationRequest;
import com.attendease.backend.studentModule.service.event.registration.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class EventRegistrationController {

    private final EventRegistrationService registrationService;

    /**
     * Endpoint for student event registration.
     *
     * @param registrationRequest  RegistrationRequest (contains eventId, locationId, latitude, longitude)
     */
    @PostMapping
    public ResponseEntity<RegistrationRequest> registerStudentToEvent(@RequestBody RegistrationRequest registrationRequest, Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        RegistrationRequest response = registrationService.eventRegistration(authenticatedUserId, registrationRequest);
        return ResponseEntity.ok(response);
    }
}

