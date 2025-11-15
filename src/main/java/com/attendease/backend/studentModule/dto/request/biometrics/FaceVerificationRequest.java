package com.attendease.backend.studentModule.dto.request.biometrics;

import java.util.List;
import lombok.Data;

@Data
public class FaceVerificationRequest {

    private List<Float> uploaded_encoding;
    private List<Float> reference_encoding;
}
