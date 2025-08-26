package com.attendease.backend.eventLocationManagement.dto;

import com.attendease.backend.eventLocationManagement.dto.request.GeoJsonRequestDTO;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationCreateGeoJsonDTO {
    private String locationName;
    private String locationType;
    private GeoJsonRequestDTO geoJsonData;
    private GeofenceParametersDTO geofenceParameters;
}
