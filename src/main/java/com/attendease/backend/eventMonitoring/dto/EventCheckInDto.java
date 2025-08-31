package com.attendease.backend.eventMonitoring.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EventCheckInDto {
    private String eventId;
    private String studentNumber;
    private LocalDateTime checkInTime;
    private String locationId;
    private double latitude;
    private double longitude;

    public EventCheckInDto(String eventId, String studentNumber, LocalDateTime checkInTime, String locationId, double latitude, double longitude) {
        this.eventId = eventId;
        this.studentNumber = studentNumber;
        this.checkInTime = checkInTime;
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
