package com.attendease.backend.studentModule.dto.response.biometrics;

import lombok.Data;

import java.util.List;

@Data
public class FaceEncodingResponse {
    private Boolean success;
    private List<String> facialEncoding;
}
