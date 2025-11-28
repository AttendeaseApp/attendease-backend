package com.attendease.backend.domain.locations.Request;

import com.attendease.backend.domain.locations.Geofencing.Geometry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class EventLocationRequest {

    @NotBlank(message = "Location name is required and cannot be blank")
    private String locationName;

    @NotNull(message = "Location type is required")
    private String locationType;

    @NotNull(message = "Geometry data is required")
    private Geometry geoJsonData;
}
