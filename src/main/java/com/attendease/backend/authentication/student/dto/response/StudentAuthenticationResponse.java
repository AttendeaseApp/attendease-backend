package com.attendease.backend.authentication.student.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAuthenticationResponse {
    private String token;
    private String userId;
    private String studentNumber;
    private String firstName;
    private String lastName;
    private String message;

    /**
     * Creates a successful authentication response
     */
    public static StudentAuthenticationResponse success(String token, String userId, String studentNumber, String firstName, String lastName) {
        return new StudentAuthenticationResponse(token, userId, studentNumber, firstName, lastName, "Authentication successful");
    }

    /**
     * Creates an error authentication response with custom message
     */
    public static StudentAuthenticationResponse error(String message) {
        return new StudentAuthenticationResponse(null, null, null, null, null, message);
    }

    /**
     * Creates an error authentication response with default message
     */
    public static StudentAuthenticationResponse error() {
        return new StudentAuthenticationResponse(null, null, null, null, null, "Authentication failed");
    }

    /**
     * Checks if this response represents a successful authentication
     */
    public boolean isSuccess() {
        return token != null && !token.trim().isEmpty();
    }

    /**
     * Checks if this response represents an authentication failure
     */
    public boolean isError() {
        return !isSuccess();
    }
}