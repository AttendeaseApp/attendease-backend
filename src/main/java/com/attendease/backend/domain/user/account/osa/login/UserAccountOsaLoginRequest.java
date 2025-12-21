package com.attendease.backend.domain.user.account.osa.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserAccountOsaLoginRequest {
    @NotBlank(message = "Email cannot be empty!")
    private String email;

    @NotBlank(message = "password cannot be empty")
    private String password;
}
