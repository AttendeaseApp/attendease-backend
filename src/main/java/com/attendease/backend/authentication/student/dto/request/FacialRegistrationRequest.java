package com.attendease.backend.authentication.student.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class FacialRegistrationRequest {
    @NotNull(message = "Face encoding is required")
    @NotEmpty(message = "Face encoding cannot be empty")
    @JsonProperty("facialEncoding")
    private List<String> facialEncoding;
}
