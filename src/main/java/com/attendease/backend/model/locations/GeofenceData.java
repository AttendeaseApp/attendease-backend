package com.attendease.backend.model.locations;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeofenceData {
    private double radiusMeters;
    private String alertType;
    private boolean isActive;
}
