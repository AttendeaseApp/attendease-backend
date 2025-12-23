package com.attendease.backend.domain.enums.location;

/**
 * Defines the intended use of a geofenced area.
 */
public enum LocationPurpose {

    /**
     * Designated for initial attendance marking.
     */
    REGISTRATION_AREA,

    /**
     * Designated for the actual event activity. Usually a larger area where
     * students are expected to stay for the duration of the event.
     */
    EVENT_VENUE
}
