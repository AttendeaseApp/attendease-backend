package com.attendease.backend.domain.records.EventCheckIn;

import lombok.Data;

@Data
public class EventCheckIn {
    private String eventId;
    private String studentNumber;
    private String locationId;
    private Double latitude;
    private Double longitude;
}
