package com.attendease.backend.studentModule.controller.event.checkin;

import com.attendease.backend.domain.records.EventCheckIn.AttendancePingLogs;
import com.attendease.backend.domain.records.EventCheckIn.RegistrationRequest;
import com.attendease.backend.studentModule.service.event.registration.EventRegistrationService;
import com.attendease.backend.studentModule.service.event.tracking.AttendanceTracking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final AttendanceTracking attendanceTracking;

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

    /**
     * Endpoint for sending periodic location pings from the client.
     * The authenticated user ID is automatically resolved from the security context.
     */
    @PostMapping("/ping")
    public ResponseEntity<?> pingAttendance(Authentication authentication, @RequestBody AttendancePingLogs attendancePingLogs) {
        try {
            String authenticatedUserId = authentication.getName();
            boolean isInside = attendanceTracking.checkpointLocationPing(authenticatedUserId, attendancePingLogs);
            return ResponseEntity.ok().body("Ping recorded successfully. Inside area: " + isInside);
        } catch (Exception ex) {
            log.error("Failed to record attendance ping: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error recording ping: " + ex.getMessage());
        }
    }
}

