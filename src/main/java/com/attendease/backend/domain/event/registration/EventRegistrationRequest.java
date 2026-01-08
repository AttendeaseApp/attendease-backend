package com.attendease.backend.domain.event.registration;

import lombok.Data;

@Data
public final class EventRegistrationRequest {

    private String eventId;
    private Double latitude;
    private Double longitude;
    private String faceImageBase64;
}
