package com.attendease.backend.exceptions.domain.Location;

public class LocationNotFoundException extends LocationException {

    private final String locationId;

    public LocationNotFoundException(String locationId) {
        super("Location not found: " + locationId);
        this.locationId = locationId;
    }

    public String getLocationId() {
        return locationId;
    }
}
