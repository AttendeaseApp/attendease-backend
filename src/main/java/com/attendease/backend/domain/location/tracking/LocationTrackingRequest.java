package com.attendease.backend.domain.location.tracking;

import lombok.Data;

/**
 * Request DTO used to validate a student's current geographic position
 * relative to a specific event location.
 *
 * <p>This object is sent by the client (usually the mobile app) and contains
 * the student's real-time latitude and longitude, as well as the target
 * event location identifier.</p>
 */
@Data
public class LocationTrackingRequest {

    private String locationId;
    private double latitude;
    private double longitude;
}
