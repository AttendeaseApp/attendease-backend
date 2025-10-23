package com.attendease.backend.studentModule.dto.response.biometrics;

import lombok.Data;

@Data
public class FaceVerificationResponse {
    private Boolean success;
    private Boolean verified;
    private Boolean is_face_matched;
    private Double confidence;
    private Double face_distance;
}
