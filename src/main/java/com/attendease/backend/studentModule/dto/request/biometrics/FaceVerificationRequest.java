package com.attendease.backend.studentModule.dto.request.biometrics;

import lombok.Data;

import java.util.List;

@Data
public class FaceVerificationRequest {
    private List<String> uploaded_encoding;
    private List<String> reference_encoding;
}
