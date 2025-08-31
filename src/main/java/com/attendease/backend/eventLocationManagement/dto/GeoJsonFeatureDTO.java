package com.attendease.backend.eventLocationManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoJsonFeatureDTO {
    private String type;
    private Map<String, Object> properties;
    private GeometryDTO geometry;
}
