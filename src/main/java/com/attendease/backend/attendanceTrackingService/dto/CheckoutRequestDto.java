package com.attendease.backend.attendanceTrackingService.dto;

import lombok.Data;

@Data
public class CheckoutRequestDto {
    private String eventId;
    private String studentNumber;
    private String locationId;
    private double latitude;
    private double longitude;
    private String checkoutTime;
}
