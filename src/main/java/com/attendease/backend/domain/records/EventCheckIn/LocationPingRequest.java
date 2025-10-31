package com.attendease.backend.domain.records.EventCheckIn;

import lombok.Data;

@Data
public class LocationPingRequest {
    private String eventId;
    private String locationId;
    private double latitude;
    private double longitude;
    private long timestamp;
}
