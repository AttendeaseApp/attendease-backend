package com.attendease.backend.domain.biometrics.Verification.Request;

import lombok.Data;

@Data
public class Base64ImageRequest {

    private String image_base64;
}
