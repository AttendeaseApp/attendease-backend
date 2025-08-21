package com.attendease.backend.authentication.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Students number cannot be empty")
    private String studentNumber;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
