package com.attendease.attendease_backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentLoginRequest {
    @NotBlank(message = "Student number cannot be empty")
    private String studentNumber;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
