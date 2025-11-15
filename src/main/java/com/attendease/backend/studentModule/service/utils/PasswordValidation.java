package com.attendease.backend.studentModule.service.utils;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating password strength and format.
 *
 * <p>This class enforces basic password rules such as minimum length,
 * maximum length, and requiring at least one alphabetic character.</p>
 */
@Component
public class PasswordValidation {

    /**
     * Validates the provided password based on predefined rules.
     *
     * <p>The validation rules include:</p>
     * <ul>
     *     <li>Password cannot be empty</li>
     *     <li>Password must be at least 6 characters long</li>
     *     <li>Password cannot exceed 128 characters</li>
     *     <li>Password must contain at least one letter (A–Z or a–z)</li>
     * </ul>
     *
     * @param password the password to validate
     *
     * @throws IllegalArgumentException if the password violates any of the rules
     */
    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password cannot exceed 128 characters");
        }
        if (!Pattern.compile("[A-Za-z]").matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }
    }
}
