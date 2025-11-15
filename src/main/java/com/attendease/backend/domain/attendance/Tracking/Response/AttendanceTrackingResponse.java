package com.attendease.backend.domain.attendance.Tracking.Response;

import lombok.Data;

@Data
public class AttendanceTrackingResponse {

    private String eventId;
    private String locationId;
    private double latitude;
    private double longitude;
    private boolean isInside;
    private long timestamp;
}
