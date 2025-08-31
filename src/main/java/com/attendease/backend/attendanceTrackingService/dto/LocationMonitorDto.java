package com.attendease.backend.attendanceTrackingService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationMonitorDto {
    private String studentNumber;
    private String eventId;
    private double latitude;
    private double longitude;
    private boolean presentAtLocation;
    private LocalDateTime lastExitTime;
    private LocalDateTime lastReturnTime;
}
