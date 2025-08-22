package com.attendease.backend.authentication.student.dto.response;

import lombok.Data;

@Data
public class FacialVerificationResponse {
    private boolean success;
    private boolean verified;
    private boolean isFaceMatched;
    private double confidence;
    private double faceDistance;
}
