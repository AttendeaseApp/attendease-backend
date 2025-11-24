package com.attendease.backend.studentModule.service.location.tracking;

import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.locations.Request.LocationTrackingRequest;
import com.attendease.backend.domain.locations.Response.LocationTrackingResponse;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.studentModule.service.utils.LocationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * {@link LocationTrackingService} responsible for checking a student's current geolocation relative to a specific event location.
 * <p>
 * This feature allows the mobile application to determine whether the student is physically
 * within an event's geofenced boundary without requiring them to be registered or checked in.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTrackingService {

    private final LocationRepository eventLocationsRepository;
    private final LocationValidator locationValidator;

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
     * @param authenticatedUserId the ID of the user attempting the location validation
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
    public LocationTrackingResponse trackCurrentLocation(String authenticatedUserId, LocationTrackingRequest request) {
        EventLocations location = eventLocationsRepository.findById(request.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));
        boolean isInside = locationValidator.isWithinLocationBoundary(location, request.getLatitude(), request.getLongitude());

        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setInside(isInside);
        response.setMessage(isInside ? "Your location has been verified. You are inside the event area." : "You are outside the event area. Please move closer to the venue.");

        return response;
    }
}
