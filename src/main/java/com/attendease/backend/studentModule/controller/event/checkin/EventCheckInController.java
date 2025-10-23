package com.attendease.backend.studentModule.controller.event.checkin;

import com.attendease.backend.studentModule.service.event.checkin.EventCheckInService;
import com.attendease.backend.domain.records.EventCheckIn.EventCheckIn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class EventCheckInController {

    private final EventCheckInService checkInService;

    /**
     * Endpoint for student event check-in.
     *
     * @param eventCheckIn  Event check-in details (contains eventId, locationId, latitude, longitude)
     * @return EventCheckInDto with confirmation details
     */
    @PostMapping
    public ResponseEntity<EventCheckIn> checkInStudent(@RequestBody EventCheckIn eventCheckIn, Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        EventCheckIn response = checkInService.checkInStudent(authenticatedUserId, eventCheckIn);
        return ResponseEntity.ok(response);
    }
}

