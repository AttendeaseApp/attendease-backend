package com.attendease.backend.eventLocationManagement.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolygonCoordinateDTO {
    private double latitude;
    private double longitude;
}
