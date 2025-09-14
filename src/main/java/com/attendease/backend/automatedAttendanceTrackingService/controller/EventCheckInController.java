package com.attendease.backend.automatedAttendanceTrackingService.controller;

import com.attendease.backend.automatedAttendanceTrackingService.service.EventCheckInService;
import com.attendease.backend.model.records.EventCheckIn.EventCheckIn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class EventCheckInController {

    private final EventCheckInService checkInService;

    /**
     * Endpoint for student event check-in.
     *
     * @param studentNumber Student identifier (e.g. student number)
     * @param eventCheckIn  Event check-in details (contains eventId, locationId, latitude, longitude)
     * @return EventCheckInDto with confirmation details
     */
    @PostMapping("/{studentNumber}")
    public ResponseEntity<EventCheckIn> checkInStudent(
            @PathVariable String studentNumber,
            @RequestBody EventCheckIn eventCheckIn) {
        try {
            EventCheckIn response = checkInService.checkInStudent(studentNumber, eventCheckIn);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.warn("Check-in failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}

