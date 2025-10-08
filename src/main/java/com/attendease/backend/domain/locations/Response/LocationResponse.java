package com.attendease.backend.domain.locations.Response;

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
}
