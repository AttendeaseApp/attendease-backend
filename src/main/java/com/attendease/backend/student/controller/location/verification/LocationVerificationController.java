package com.attendease.backend.student.controller.location.verification;

import com.attendease.backend.domain.location.tracking.EventLocationTrackingRequest;
import com.attendease.backend.domain.location.tracking.LocationTrackingRequest;
import com.attendease.backend.domain.location.tracking.LocationTrackingResponse;
import com.attendease.backend.student.service.location.verification.impl.LocationVerificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time location verification.
 * <p>
 * Provides endpoints for students to verify their location against:
 * <ul>
 *   <li>Generic location boundaries (any location)</li>
 *   <li>Event registration location (for check-in phase)</li>
 *   <li>Event venue location (for ongoing monitoring during event)</li>
 * </ul>
 * </p>
 * <p>
 * All responses are sent only to the requesting user via {@code @SendToUser},
 * ensuring privacy and preventing broadcast of location data to other users.
 * </p>
 */
@Controller
@RequiredArgsConstructor
public class LocationVerificationController {

    private final LocationVerificationServiceImpl locationVerificationService;

//    /**
//     * Receives the student's geolocation via WebSocket and returns whether
//     * they are inside or outside the specified location boundary.
//     * <p>
//     * This is a generic endpoint that works with any location ID.
//     * Each user receives only their own result (not broadcasted).
//     * </p>
//     *
//     * @param request contains locationId, latitude, and longitude
//     * @return verification response indicating if student is inside/outside
//     */
//    @MessageMapping("/observe-current-location")
//    @SendToUser("/queue/location-verification")
//    public LocationTrackingResponse verifyCurrentLocation(@Payload LocationTrackingRequest request) {
//        return locationVerificationService.verifyCurrentLocation(request);
//    }

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
    @MessageMapping("/verify-registration-location")
    @SendToUser("/queue/registration-location-verification")
    public LocationTrackingResponse verifyEventRegistrationLocation(@Payload EventLocationTrackingRequest request) {
        return locationVerificationService.verifyEventRegistrationLocation(
                request.getEventId(),
                request.getLatitude(),
                request.getLongitude()
        );
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
    @MessageMapping("/verify-venue-location")
    @SendToUser("/queue/venue-location-verification")
    public LocationTrackingResponse verifyEventVenueLocation(@Payload EventLocationTrackingRequest request) {
        return locationVerificationService.verifyEventVenueLocation(
                request.getEventId(),
                request.getLatitude(),
                request.getLongitude()
        );
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
    @MessageMapping("/verify-venue-location-with-upgrade")
    @SendToUser("/queue/venue-location-auto-upgrade")
    public LocationTrackingResponse verifyEventVenueLocationWithAutoUpgrade(@Payload EventLocationTrackingRequest request, Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        return locationVerificationService.verifyEventVenueLocationWithAutoUpgrade(authenticatedUserId,request.getEventId(),request.getLatitude(),request.getLongitude());
    }
}