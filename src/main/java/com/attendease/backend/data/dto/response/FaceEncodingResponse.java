package com.attendease.backend.data.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FaceEncodingResponse {
    private boolean success;
    private String message;
    @JsonProperty("facialEncoding")
    private List<Double> facialEncoding;
    private String error;
}