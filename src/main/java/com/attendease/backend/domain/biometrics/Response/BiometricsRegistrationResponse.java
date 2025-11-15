package com.attendease.backend.domain.biometrics.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class BiometricsRegistrationResponse {

    private boolean success;
    private String message;

    @JsonProperty("facialEncoding")
    private List<Float> facialEncoding;

    private String error;
}
