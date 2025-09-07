package com.attendease.backend.eventAttendanceMonitoringService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EventCheckInDto {
    private String eventId;
    private String studentNumber;
    private String locationId;
    private Double latitude;
    private Double longitude;

    public EventCheckInDto(String eventId, String studentNumber, String locationId, Double latitude, Double longitude) {
        this.eventId = eventId;
        this.studentNumber = studentNumber;
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
