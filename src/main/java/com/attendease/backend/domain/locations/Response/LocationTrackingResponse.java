package com.attendease.backend.domain.locations.Response;

import lombok.Data;

/**
 * Response DTO returned after validating the student's real-time position
 * against a specific event location boundary.
 *
 * <p>Indicates whether the student is inside the geofence and includes a
 * user-friendly message explaining the result.</p>
 */
@Data
public class LocationTrackingResponse {

    private boolean isInside;
    private String message;
}
