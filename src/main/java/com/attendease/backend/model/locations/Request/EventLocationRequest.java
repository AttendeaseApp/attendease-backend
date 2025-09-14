package com.attendease.backend.model.locations.Request;

import com.attendease.backend.model.locations.Geofencing.GeofenceData;
import com.attendease.backend.model.locations.Geofencing.Geometry;
import lombok.Data;


@Data
public class EventLocationRequest {
    private String locationName;
    private String locationType;
    private Geometry geoJsonData;
    private GeofenceData geofenceParameters;
}
