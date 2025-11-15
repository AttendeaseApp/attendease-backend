package com.attendease.backend.studentModule.service.utils;

import java.util.regex.Pattern;

public class PasswordValidation {

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
