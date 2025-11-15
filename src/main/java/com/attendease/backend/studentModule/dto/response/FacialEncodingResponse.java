package com.attendease.backend.studentModule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class FacialEncodingResponse {

    private boolean success;
    private String message;

    @JsonProperty("facialEncoding")
    private List<Float> facialEncoding;

    private String error;
}
