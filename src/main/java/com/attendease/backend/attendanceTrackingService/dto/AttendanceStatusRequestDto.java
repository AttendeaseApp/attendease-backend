package com.attendease.backend.attendanceTrackingService.dto;

import lombok.Data;

@Data
public class AttendanceStatusRequestDto {
    private String eventId;
    private String studentNumber;
    private String locationId;
    private String status; // PRESENT or ABSENT
    private String timestamp;
}
