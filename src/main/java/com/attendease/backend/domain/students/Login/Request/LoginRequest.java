package com.attendease.backend.domain.students.Login.Request;

import lombok.Data;

/**
 * Request DTO used when a student attempts to log into the system.
 *
 * <p>This object contains the student's login credentials.
 * Validation (if needed) should be applied at the controller layer.</p>
 */
@Data
public class LoginRequest {

    public String studentNumber;
    public String password;
}
