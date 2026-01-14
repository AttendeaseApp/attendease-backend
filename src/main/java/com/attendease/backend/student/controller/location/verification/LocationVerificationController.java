package com.attendease.backend.student.controller.location.verification;

import com.attendease.backend.domain.location.tracking.EventLocationTrackingRequest;
import com.attendease.backend.domain.location.tracking.LocationTrackingResponse;
import com.attendease.backend.student.service.location.verification.impl.LocationVerificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller for location verification.
 * <p>
 * Provides endpoints for students to verify their location against:
 * <ul>
 *   <li>Generic location boundaries (any location)</li>
 *   <li>Event registration location (for check-in phase)</li>
 *   <li>Event venue location (for ongoing monitoring during event)</li>
 * </ul>
 * </p>
 * <p>
 * All endpoints require authentication and return location verification
 * results specific to the authenticated user.
 * </p>
 */
@RestController
@RequestMapping("/api/student/location/verification")
@RequiredArgsConstructor
public class LocationVerificationController {

    private final LocationVerificationServiceImpl locationVerificationService;

    /**
     * Verifies if the student is within the event's registration location.
     * <p>
     * This endpoint is used during the check-in phase to validate that the student
     * is physically present at the designated registration area before allowing
     * them to complete event registration.
     * </p>
     * <p>
     * <b>Use Case:</b> Pre-check before displaying the check-in button/form
     * </p>
     *
     * @param request contains eventId, latitude, and longitude
     * @return verification response for registration location
     */
    @PostMapping("/registration-location")
    public ResponseEntity<LocationTrackingResponse> verifyEventRegistrationLocation(@RequestBody EventLocationTrackingRequest request) {
        LocationTrackingResponse response = locationVerificationService.verifyEventRegistrationLocation(
                request.getEventId(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies if the student is within the event's venue location.
     * <p>
     * This endpoint is used during the event session to validate that the student
     * remains within the venue boundaries. This is typically called as a pre-check
     * before sending attendance tracking pings.
     * </p>
     * <p>
     * <b>Use Case:</b> Real-time venue boundary validation during ongoing events
     * </p>
     *
     * @param request contains eventId, latitude, and longitude
     * @return verification response for venue location
     */
    @PostMapping("/venue-location")
    public ResponseEntity<LocationTrackingResponse> verifyEventVenueLocation(@RequestBody EventLocationTrackingRequest request) {
        LocationTrackingResponse response = locationVerificationService.verifyEventVenueLocation(
                request.getEventId(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies if the student is within the event's venue location AND automatically
     * upgrades PARTIALLY_REGISTERED students to REGISTERED status.
     * <p>
     * This endpoint is specifically designed for strict location validation mode.
     * When a student with PARTIALLY_REGISTERED status enters the venue geofence,
     * their status is automatically upgraded to REGISTERED (or LATE if after event start).
     * </p>
     */
    @PostMapping("/venue-location/auto-upgrade")
    public ResponseEntity<LocationTrackingResponse> verifyEventVenueLocationWithAutoUpgrade(@RequestBody EventLocationTrackingRequest request, Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        LocationTrackingResponse response = locationVerificationService.verifyEventVenueLocationWithAutoUpgrade(
                authenticatedUserId,
                request.getEventId(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.ok(response);
    }
}