package com.attendease.backend.student.service.location.verification.impl;

import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.locations.Request.LocationTrackingRequest;
import com.attendease.backend.domain.locations.Response.LocationTrackingResponse;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.student.service.location.verification.LocationVerificationService;
import com.attendease.backend.student.service.utils.LocationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationVerificationServiceImpl implements LocationVerificationService {

    private final LocationRepository eventLocationsRepository;
    private final LocationValidator locationValidator;

    @Override
    public LocationTrackingResponse trackCurrentLocation(LocationTrackingRequest request) {
        EventLocations location = eventLocationsRepository.findById(request.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));
        boolean isInside = locationValidator.isWithinLocationBoundary(location, request.getLatitude(), request.getLongitude());
        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setInside(isInside);
        response.setMessage(isInside ? "Your location has been verified. You are inside the event area." : "You are outside the event area. Please move closer to the venue.");
        return response;
    }
}
