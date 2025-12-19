package com.attendease.backend.domain.student.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after a student login attempt.
 *
 * <p>This object includes authentication results such as tokens,
 * additional requirements (e.g., facial registration), and related metadata.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private boolean success;
    private String token;
    private boolean requiresFacialRegistration;
    private String message;
    private String studentNumber;
}
