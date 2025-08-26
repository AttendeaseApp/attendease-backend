package com.attendease.backend.eventLocationManagement.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoJsonFeatureDTO {
    private String type; // "Feature"
    private Map<String, Object> properties;
    private GeometryDTO geometry;
}
