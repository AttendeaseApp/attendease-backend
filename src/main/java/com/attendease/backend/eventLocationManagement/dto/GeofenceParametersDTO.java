package com.attendease.backend.eventLocationManagement.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeofenceParametersDTO {
    private double radiusMeters;
    private double centerLatitude;
    private double centerLongitude;
    private String alertType;
    private boolean isActive;
}
