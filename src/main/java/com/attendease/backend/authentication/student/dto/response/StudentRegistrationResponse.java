package com.attendease.backend.authentication.student.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student registration operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRegistrationResponse {
    private boolean success;
    private String studentNumber;
    private String firstName;
    private String lastName;
    private String message;
    private String errorCode;

    /**
     * Creates a successful registration response
     */
    public static StudentRegistrationResponse success(String studentNumber, String firstName, String lastName, String message) {
        return new StudentRegistrationResponse(true, studentNumber, firstName, lastName, message, null);
    }

    /**
     * Creates an error registration response
     */
    public static StudentRegistrationResponse error(String message) {
        return new StudentRegistrationResponse(false, null, null, null, message, "REGISTRATION_ERROR");
    }
}
