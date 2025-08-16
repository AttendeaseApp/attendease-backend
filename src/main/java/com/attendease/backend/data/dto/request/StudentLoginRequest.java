package com.attendease.backend.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentLoginRequest {
    @NotBlank(message = "Students number cannot be empty")
    private String studentNumber;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
