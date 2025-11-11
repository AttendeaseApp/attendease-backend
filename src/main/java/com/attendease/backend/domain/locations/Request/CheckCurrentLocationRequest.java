package com.attendease.backend.domain.locations.Request;

import lombok.Data;

@Data
public class CheckCurrentLocationRequest {
    private String locationId;
    private double latitude;
    private double longitude;
}
