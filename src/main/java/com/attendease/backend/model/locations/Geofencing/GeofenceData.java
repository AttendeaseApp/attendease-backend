package com.attendease.backend.model.locations.Geofencing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representing geofence data for a location.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeofenceData {
    private Double radiusMeters;
    private Double centerLatitude;
    private Double centerLongitude;
}
