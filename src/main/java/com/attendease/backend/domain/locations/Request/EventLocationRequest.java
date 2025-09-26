package com.attendease.backend.domain.locations.Request;

import com.attendease.backend.domain.locations.Geofencing.Geometry;
import lombok.Data;


@Data
public class EventLocationRequest {
    private String locationName;
    private String locationType;
    private Geometry geoJsonData;
}
