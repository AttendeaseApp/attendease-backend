package com.attendease.backend.student.controller.location.verification;

import com.attendease.backend.domain.location.tracking.EventLocationTrackingRequest;
import com.attendease.backend.domain.location.tracking.LocationTrackingRequest;
import com.attendease.backend.domain.location.tracking.LocationTrackingResponse;
import com.attendease.backend.student.service.location.verification.impl.LocationVerificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
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

    /**
     * Receives the student's geolocation via WebSocket and returns whether
     * they are inside or outside the specified location boundary.
     * <p>
     * This is a generic endpoint that works with any location ID.
     * Each user receives only their own result (not broadcasted).
     * </p>
     *
     * @param request contains locationId, latitude, and longitude
     * @return verification response indicating if student is inside/outside
     */
    @MessageMapping("/observe-current-location")
    @SendToUser("/queue/location-verification")
    public LocationTrackingResponse trackCurrentLocation(@Payload LocationTrackingRequest request) {
        return locationVerificationService.trackCurrentLocation(request);
    }

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
    public LocationTrackingResponse verifyRegistrationLocation(@Payload EventLocationTrackingRequest request) {
        return locationVerificationService.trackEventRegistrationLocation(
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
    public LocationTrackingResponse verifyVenueLocation(@Payload EventLocationTrackingRequest request) {
        return locationVerificationService.trackEventVenueLocation(
                request.getEventId(),
                request.getLatitude(),
                request.getLongitude()
        );
    }
}