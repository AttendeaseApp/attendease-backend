package com.attendease.backend.authentication.student.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password change requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {
    private String studentNumber;
    private String oldPassword;
    private String newPassword;

    /**
     * Validates that all password change fields are present
     * @return true if all fields are provided
     */
    public boolean isValid() {
        return studentNumber != null && !studentNumber.trim().isEmpty() && oldPassword != null && !oldPassword.trim().isEmpty() && newPassword != null && !newPassword.trim().isEmpty();
    }
}
