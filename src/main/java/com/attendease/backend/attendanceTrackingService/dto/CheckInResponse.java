package com.attendease.backend.attendanceTrackingService.dto;

import lombok.Data;

@Data
public class CheckInResponse {
    private boolean success;
    private String message;
    private Object data;

    public CheckInResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public CheckInResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }
}
