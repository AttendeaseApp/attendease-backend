package com.attendease.backend.student.service.location.verification;

import com.attendease.backend.domain.location.tracking.LocationTrackingRequest;
import com.attendease.backend.domain.location.tracking.LocationTrackingResponse;
import com.attendease.backend.student.service.location.verification.impl.LocationVerificationServiceImpl;
import com.attendease.backend.student.service.utils.LocationValidator;

/**
 * {@link LocationVerificationServiceImpl} responsible for checking a student's current geolocation relative to a specific event location.
 * <p>
 * This feature allows the mobile application to determine whether the student is physically
 * within an event's geofenced boundary without requiring them to be registered or checked in.
 * </p>
 */
public interface LocationVerificationService {

    /**
     * Checks whether the authenticated student's current GPS location falls inside a specified event location.
     * <p>
     * This method:
     * <ul>
     *     <li>Validates the authenticated user and associated student record</li>
     *     <li>Retrieves the target event location</li>
     *     <li>Uses {@link LocationValidator} to determine positional accuracy</li>
     *     <li>Returns a response object describing whether the student is inside or outside the boundary</li>
     * </ul>
     * </p>
     *
     * @param request             the request payload containing latitude, longitude, and location ID
     * @return a {@link LocationTrackingResponse} containing boundary status and a user-friendly message
     *
     * @throws IllegalStateException if:
     *         <ul>
     *             <li>The user cannot be found</li>
     *             <li>The student profile associated with the user does not exist</li>
     *             <li>The target location does not exist</li>
     *         </ul>
     */
    LocationTrackingResponse trackCurrentLocation(LocationTrackingRequest request);

    /**
     * Verifies if student is within the event's venue location.
     * This is specifically for ongoing monitoring during the event.
     *
     * @param eventId the event ID
     * @param latitude student's current latitude
     * @param longitude student's current longitude
     * @return tracking response with verification status
     */
    LocationTrackingResponse trackEventVenueLocation(String eventId, double latitude, double longitude);

    /**
     * Verifies if student is within the event's registration location.
     * This is used during the registration/check-in phase.
     *
     * @param eventId the event ID
     * @param latitude student's current latitude
     * @param longitude student's current longitude
     * @return tracking response with verification status
     */
    LocationTrackingResponse trackEventRegistrationLocation(String eventId, double latitude, double longitude);
}
