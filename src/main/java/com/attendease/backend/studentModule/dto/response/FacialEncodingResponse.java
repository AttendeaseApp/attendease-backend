package com.attendease.backend.studentModule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FacialEncodingResponse {
    private boolean success;
    private String message;
    @JsonProperty("facialEncoding")
    private List<Double> facialEncoding;
    private String error;
}