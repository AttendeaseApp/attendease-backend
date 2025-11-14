package com.attendease.backend.studentModule.dto.request.biometrics;

import lombok.Data;

import java.util.List;

@Data
public class FaceVerificationRequest {
    private List<Double> uploaded_encoding;
    private List<Double> reference_encoding;
}
