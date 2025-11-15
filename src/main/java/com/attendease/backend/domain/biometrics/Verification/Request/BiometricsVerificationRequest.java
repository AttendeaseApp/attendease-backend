package com.attendease.backend.domain.biometrics.Verification.Request;

import java.util.List;
import lombok.Data;

@Data
public class BiometricsVerificationRequest {

    private List<Float> uploaded_encoding;
    private List<Float> reference_encoding;
}
