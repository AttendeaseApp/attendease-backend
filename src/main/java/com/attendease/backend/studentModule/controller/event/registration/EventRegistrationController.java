package com.attendease.backend.studentModule.controller.event.registration;

import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.events.Registration.Request.EventRegistrationRequest;
import com.attendease.backend.domain.locations.Request.LocationTrackingRequest;
import com.attendease.backend.domain.locations.Response.LocationTrackingResponse;
import com.attendease.backend.studentModule.service.attendance.tracking.AttendanceTrackingService;
import com.attendease.backend.studentModule.service.event.registration.EventRegistrationService;
import com.attendease.backend.studentModule.service.location.tracking.LocationTrackingService;
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
    private final AttendanceTrackingService attendanceTrackingService;
    private final LocationTrackingService locationTrackingService;

    /**
     * Endpoint for student event registration.
     *
     * @param registrationRequest  RegistrationRequest (contains eventId, locationId, latitude, longitude)
     */
    @PostMapping
    public ResponseEntity<?> registerStudentToEvent(@RequestBody EventRegistrationRequest registrationRequest, Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        EventRegistrationRequest response = registrationService.eventRegistration(authenticatedUserId, registrationRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for sending periodic location pings from the client.
     * The authenticated user ID is automatically resolved from the security context.
     */
    @PostMapping("/ping")
    public ResponseEntity<?> pingAttendance(Authentication authentication, @RequestBody AttendanceTrackingResponse attendancePingLogs) {
        String authenticatedUserId = authentication.getName();
        boolean isInside = attendanceTrackingService.attendanceMonitoringLocationPings(authenticatedUserId, attendancePingLogs);
        return ResponseEntity.ok().body("Ping recorded successfully. Inside area: " + isInside);
    }

    /**
     * Endpoint for verifying students current location used for ui visuals only
     * The authenticated user ID is automatically resolved from the security context.
     */
    @PostMapping("/check-location")
    public ResponseEntity<LocationTrackingResponse> trackCurrentLocationOfStudent(Authentication authentication, @RequestBody LocationTrackingRequest request) {
        String userId = authentication.getName();
        LocationTrackingResponse response = locationTrackingService.trackCurrentLocation(userId, request);
        return ResponseEntity.ok(response);
    }
}
