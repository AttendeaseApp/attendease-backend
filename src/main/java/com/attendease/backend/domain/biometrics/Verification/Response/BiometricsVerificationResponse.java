package com.attendease.backend.domain.biometrics.Verification.Response;

import lombok.Data;

@Data
public class BiometricsVerificationResponse {

    private Boolean success;
    private Boolean verified;
    private Boolean is_face_matched;
}
