package com.attendease.backend.domain.attendance.EventRegistration;

import lombok.Data;

@Data
public class EventRegistrationRequest {

    private String eventId;
    private String locationId;
    private Double latitude;
    private Double longitude;
    private String faceImageBase64;
}
