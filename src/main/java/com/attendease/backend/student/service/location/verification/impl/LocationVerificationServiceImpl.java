package com.attendease.backend.student.service.location.verification.impl;

import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.location.Location;
import com.attendease.backend.domain.location.tracking.LocationTrackingRequest;
import com.attendease.backend.domain.location.tracking.LocationTrackingResponse;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.location.LocationRepository;
import com.attendease.backend.student.service.location.verification.LocationVerificationService;
import com.attendease.backend.student.service.utils.LocationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for verifying student location against event venues.
 * <p>
 * This service handles real-time location verification for ongoing events,
 * checking if students are within the venue location boundaries during
 * the event session (not during registration).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationVerificationServiceImpl implements LocationVerificationService {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final LocationValidator locationValidator;

    @Override
    public LocationTrackingResponse trackCurrentLocation(LocationTrackingRequest request) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalStateException("Location not found"));

        boolean isInside = locationValidator.isWithinLocationBoundary(
                location,
                request.getLatitude(),
                request.getLongitude());

        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setInside(isInside);
        response.setMessage(isInside
                ? String.format("Your location has been verified. You are inside %s.", location.getLocationName())
                : String.format("You are outside %s. Please move closer to the venue.", location.getLocationName()));

        log.info("Location verification for location {}: Student is {}",
                location.getLocationName(),
                isInside ? "INSIDE" : "OUTSIDE");

        return response;
    }

    @Override
    public LocationTrackingResponse trackEventVenueLocation(String eventId, double latitude, double longitude) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Event not found"));

        Location venueLocation = event.getVenueLocation();
        if (venueLocation == null) {
            log.error("Event {} has no venue location configured", eventId);
            throw new IllegalStateException("Event venue location is not configured");
        }

        boolean isInside = locationValidator.isWithinLocationBoundary(
                venueLocation,
                latitude,
                longitude);

        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setInside(isInside);
        response.setMessage(isInside
                ? String.format("You are inside the event venue (%s).", venueLocation.getLocationName())
                : String.format("Warning: You are outside the event venue (%s). Please return to the venue area.",
                venueLocation.getLocationName()));

        log.info("Venue location tracking for event {}: Student is {} venue {}",
                eventId,
                isInside ? "INSIDE" : "OUTSIDE",
                venueLocation.getLocationName());

        return response;
    }

    @Override
    public LocationTrackingResponse trackEventRegistrationLocation(String eventId, double latitude, double longitude) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Event not found"));

        Location registrationLocation = event.getRegistrationLocation();
        if (registrationLocation == null) {
            log.error("Event {} has no registration location configured", eventId);
            throw new IllegalStateException("Event registration location is not configured");
        }

        boolean isInside = locationValidator.isWithinLocationBoundary(
                registrationLocation,
                latitude,
                longitude);

        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setInside(isInside);
        response.setMessage(isInside
                ? String.format("You are at the registration location (%s). You may proceed with check-in.",
                registrationLocation.getLocationName())
                : String.format("You must be at the registration location (%s) to check in for this event.",
                registrationLocation.getLocationName()));

        log.info("Registration location tracking for event {}: Student is {} registration area {}",
                eventId,
                isInside ? "INSIDE" : "OUTSIDE",
                registrationLocation.getLocationName());

        return response;
    }
}