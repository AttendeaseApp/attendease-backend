package com.attendease.backend.eventLocationManagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeofenceParametersDTO {
    private double radiusMeters;
    private double centerLatitude;
    private double centerLongitude;

    public GeofenceParametersDTO(double radiusMeters, double centerLatitude, double centerLongitude) {
        this.radiusMeters = radiusMeters;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
    }
}
