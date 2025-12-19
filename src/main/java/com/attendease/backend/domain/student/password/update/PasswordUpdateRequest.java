package com.attendease.backend.domain.student.password.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO used for updating a student's password.
 *
 * <p>This object contains the old password (for verification) and the
 * new password, which will be validated and applied if valid.</p>
 *
 * <p>Both fields are required and validated using {@link NotBlank}.</p>
 */
@Data
public class PasswordUpdateRequest {

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
