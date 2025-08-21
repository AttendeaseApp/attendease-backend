package com.attendease.backend.data.dto.response;

import lombok.Data;

@Data
public class FaceVerificationResponse {
    private boolean success;
    private boolean verified;
    private boolean isFaceMatched;
    private double confidence;
    private double faceDistance;
}
