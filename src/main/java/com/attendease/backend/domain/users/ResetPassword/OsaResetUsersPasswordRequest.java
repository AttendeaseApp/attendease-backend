package com.attendease.backend.domain.users.ResetPassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OsaResetUsersPasswordRequest {

    @NotBlank(message = "New password is required")
    private String newPassword;
}

