package com.attendease.backend.domain.biometrics.Response;

import lombok.Data;

@Data
public class BiometricsServiceVerificationResponse {

    private Boolean success;
    private Boolean verified;
    private Boolean is_face_matched;
}
