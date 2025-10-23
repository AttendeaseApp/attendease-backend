package com.attendease.backend.studentModule.dto.request.biometrics;

import lombok.Data;

@Data
public class FaceImageRequest {
    private String image_base64;
}
