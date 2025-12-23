package com.attendease.backend.domain.enums.location;

/**
 * Describes the physical setting of a location.
 * Used to calibrate geofencing sensitivity and inform users of potential
 * GPS accuracy issues.
 */
public enum LocationEnvironment {

    /**
     * Located inside a building or rooms. Expects a very low GPS precision.
     */
    INDOOR,

    /**
     * Located in an open area. Expects high GPS precision.
     */
    OUTDOOR
}
