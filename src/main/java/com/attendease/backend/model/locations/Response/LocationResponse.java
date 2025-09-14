package com.attendease.backend.model.locations.Response;

import com.attendease.backend.model.locations.Geofencing.GeofenceData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private String locationId;
    private String locationName;
    private String locationType;
    private Double latitude;
    private Double longitude;
    private GeofenceData geofenceParameters;
}
