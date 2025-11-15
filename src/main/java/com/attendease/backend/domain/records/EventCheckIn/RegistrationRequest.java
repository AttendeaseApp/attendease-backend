package com.attendease.backend.domain.records.EventCheckIn;

import lombok.Data;

@Data
public class RegistrationRequest {

    private String eventId;
    private String locationId;
    private Double latitude;
    private Double longitude;
    private String faceImageBase64;
}
