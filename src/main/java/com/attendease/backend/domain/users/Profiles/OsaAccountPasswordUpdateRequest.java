package com.attendease.backend.domain.users.Profiles;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OsaAccountPasswordUpdateRequest {
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
