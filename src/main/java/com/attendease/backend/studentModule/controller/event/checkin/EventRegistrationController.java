package com.attendease.backend.studentModule.controller.event.checkin;

import com.attendease.backend.domain.records.EventCheckIn.AttendancePingLogs;
import com.attendease.backend.domain.records.EventCheckIn.RegistrationRequest;
import com.attendease.backend.studentModule.service.event.registration.EventRegistrationService;
import com.attendease.backend.studentModule.service.event.tracking.AttendanceTracking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<?> registerStudentToEvent(@RequestBody RegistrationRequest registrationRequest, Authentication authentication) {
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
        String authenticatedUserId = authentication.getName();
        boolean isInside = attendanceTracking.checkpointLocationPing(authenticatedUserId, attendancePingLogs);
        return ResponseEntity.ok().body("Ping recorded successfully. Inside area: " + isInside);
    }
}

