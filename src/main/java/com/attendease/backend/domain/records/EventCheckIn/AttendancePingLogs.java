package com.attendease.backend.domain.records.EventCheckIn;

import lombok.Data;

@Data
public class AttendancePingLogs {
    private String eventId;
    private String locationId;
    private double latitude;
    private double longitude;
    private boolean isInside;
    private long timestamp;
}
