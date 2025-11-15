package com.attendease.backend.studentModule.dto.response.biometrics;

import java.util.List;
import lombok.Data;

@Data
public class FaceEncodingResponse {

    private Boolean success;
    private List<Float> facialEncoding;
}
