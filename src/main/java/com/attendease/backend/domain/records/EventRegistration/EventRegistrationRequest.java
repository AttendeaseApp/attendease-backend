package com.attendease.backend.domain.records.EventRegistration;

import lombok.Data;

@Data
public class EventRegistrationRequest {

    private String eventId;
    private String locationId;
    private Double latitude;
    private Double longitude;
    private String faceImageBase64;
}
