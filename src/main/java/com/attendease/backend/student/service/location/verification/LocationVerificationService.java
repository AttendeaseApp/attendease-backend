package com.attendease.backend.student.service.location.verification;

import com.attendease.backend.domain.location.tracking.LocationTrackingResponse;
import com.attendease.backend.student.service.location.verification.impl.LocationVerificationServiceImpl;

/**
 * {@link LocationVerificationServiceImpl} responsible for checking a student's current geolocation relative to a specific event location.
 * <p>
 * This feature allows the mobile application to determine whether the student is physically
 * within an event's geofenced boundary without requiring them to be registered or checked in.
 * </p>
 */
public interface LocationVerificationService {

    /**
     * Verifies if student is within the event's venue location.
     * This is specifically for ongoing monitoring during the event.
     *
     * @param eventId the event ID
     * @param latitude student's current latitude
     * @param longitude student's current longitude
     * @return tracking response with verification status
     */
    LocationTrackingResponse verifyEventVenueLocation(String eventId, double latitude, double longitude);

    /**
     * Verifies if student is within the event's registration location.
     * This is used during the registration/check-in phase.
     *
     * @param eventId the event ID
     * @param latitude student's current latitude
     * @param longitude student's current longitude
     * @return tracking response with verification status
     */
    LocationTrackingResponse verifyEventRegistrationLocation(String eventId, double latitude, double longitude);

    LocationTrackingResponse verifyEventVenueLocationWithAutoUpgrade(
            String authenticatedUserId,
            String eventId,
            double latitude,
            double longitude);
}
