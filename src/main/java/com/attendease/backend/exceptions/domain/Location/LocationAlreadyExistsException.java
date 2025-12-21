package com.attendease.backend.exceptions.domain.Location;

import com.attendease.backend.domain.enums.location.LocationEnvironment;
import com.attendease.backend.domain.enums.location.LocationPurpose;

public class LocationAlreadyExistsException extends LocationException {

    private final String locationName;
    private final LocationEnvironment environment;
    private final LocationPurpose purpose;

    public LocationAlreadyExistsException(String locationName, LocationEnvironment environment, LocationPurpose purpose) {
        super(String.format("Location '%s' (%s, %s) already exists", locationName, environment, purpose));
        this.locationName = locationName;
        this.environment = environment;
        this.purpose = purpose;
    }

    public String getLocationName() {
        return locationName;
    }

    public LocationEnvironment getEnvironment() {
        return environment;
    }

    public LocationPurpose getPurpose() {
        return purpose;
    }
}
