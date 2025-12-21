package com.attendease.backend.exceptions.domain.Location;

public class LocationHasActiveEventsException extends LocationException {

    private final String locationName;
    private final String blockedStatusesSummary;

    public LocationHasActiveEventsException(String locationName, String blockedStatusesSummary) {
        super(String.format(
                "Cannot update location '%s' as it is used by event session(s) with blocked statuses (%s). " +
                        "This would affect attendance record integrity. Cancel or reassign those events first.",
                locationName, blockedStatusesSummary
        ));
        this.locationName = locationName;
        this.blockedStatusesSummary = blockedStatusesSummary;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getBlockedStatusesSummary() {
        return blockedStatusesSummary;
    }
}
