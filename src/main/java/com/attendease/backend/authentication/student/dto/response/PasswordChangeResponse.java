package com.attendease.backend.authentication.student.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EventCreation DTO for password change operations
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeResponse {
    private boolean success;
    private String message;
    private String errorCode;
}
