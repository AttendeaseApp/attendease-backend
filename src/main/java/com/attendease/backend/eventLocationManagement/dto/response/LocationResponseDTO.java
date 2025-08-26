package com.attendease.backend.eventLocationManagement.dto.response;

import com.attendease.backend.eventLocationManagement.dto.GeofenceParametersDTO;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponseDTO {
    private String locationId;
    private String locationName;
    private String locationType;
    private double latitude;
    private double longitude;
    private GeofenceParametersDTO geofenceParameters;
}
