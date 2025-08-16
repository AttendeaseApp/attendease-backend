package com.attendease.backend.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OsaLoginRequest {
    @NotBlank(message = "Email cannot be empty!")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
