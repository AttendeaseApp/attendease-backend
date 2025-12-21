package com.attendease.backend.validation;

import com.attendease.backend.validation.rules.UserValidationRules;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    private static final int FIRSTNAME_MIN = 2;
    private static final int FIRSTNAME_MAX = 50;

    private static final int LASTNAME_MIN = 2;
    private static final int LASTNAME_MAX = 50;

    private static final int PASSWORD_MIN = 6;
    private static final int PASSWORD_MAX = 128;

    public void validateFirstName(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (value.length() < FIRSTNAME_MIN || value.length() > FIRSTNAME_MAX) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + FIRSTNAME_MIN + " and " + FIRSTNAME_MAX + " characters."
            );
        }
        if (!value.matches(UserValidationRules.FIRSTNAME_FORMAT_REGEX)) {
            throw new IllegalArgumentException(
                    fieldName + " contains invalid characters. Only letters, spaces, apostrophes, and hyphens are allowed."
            );
        }
    }

    public void validateLastName(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (value.length() < LASTNAME_MIN || value.length() > LASTNAME_MAX) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + LASTNAME_MIN + " and " + LASTNAME_MAX + " characters."
            );
        }
        if (!value.matches(UserValidationRules.LASTNAME_FORMAT_REGEX)) {
            throw new IllegalArgumentException(
                    fieldName + " contains invalid characters. Only letters, spaces, apostrophes, and hyphens are allowed."
            );
        }
    }

    public void validatePassword(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("password is required.");
        }
        if (value.length() < PASSWORD_MIN || value.length() > PASSWORD_MAX) {
            throw new IllegalArgumentException(
                    "password must be between " + PASSWORD_MIN + " and " + PASSWORD_MAX + " characters."
            );
        }
        if (!value.matches(UserValidationRules.PASSWORD_FORMAT_REGEX)) {
            throw new IllegalArgumentException(
                    "password must contain uppercase, lowercase, digit, special character."
            );
        }
    }

    public void validateContactNumber(String value) {
        if (value != null && !value.matches(UserValidationRules.CONTACT_NUMBER_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Invalid contact number format.");
        }
    }

    public void validateEmail(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!value.matches(UserValidationRules.EMAIL_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    public void validateStudentNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required.");
        }
        if (!value.matches(UserValidationRules.STUDENT_NUMBER_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Invalid student number format (expected CTyy-xxxx).");
        }
    }

    /**
     * Validates the full course-section identifier format.
     *
     * @param fullIdentifier The full identifier to validate (e.g., "BSIT-101").
     *
     * @throws IllegalArgumentException If the format is invalid.
     */
    public void validateFullCourseSectionFormat(String fullIdentifier) {
        if (fullIdentifier == null || fullIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Section name is required.");
        }
        if (!fullIdentifier.matches(UserValidationRules.SECTION_NAME_FORMAT_REGEX)) {
            throw new IllegalArgumentException(
                    "Invalid section format. Expected format: NAME-XXX (e.g., BSIT-101). " + "COURSE must be uppercase letters or numbers, and section number must be exactly 3 digits."
            );
        }
    }

    /**
     * Validates the full course name identifier format.
     *
     * @param courseName The full identifier to validate (e.g., "BSIT").
     *
     * @throws IllegalArgumentException If the format is invalid.
     */
    public void validateCourseNameFormat(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required.");
        }
        if (!courseName.matches(UserValidationRules.COURSE_NAME_FORMAT_REGEX)) {
            throw new IllegalArgumentException(
                    "Invalid course name '" + courseName + "'. Only uppercase letters, digits, and dashes are allowed, without spaces."
            );
        }
    }

    /**
     * Validates cluster name format.
     *
     * <p>Cluster names must be uppercase letters, digits, and optionally dashes. Spaces are not allowed.</p>
     *
     * @param name Cluster name to validate.
     * @throws IllegalArgumentException if invalid.
     */
    public void validateClusterNameFormat(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cluster name is required.");
        }
        if (!name.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException(
                    "Invalid cluster name '" + name + "'. Only uppercase letters, digits, and dashes are allowed, without spaces."
            );
        }
    }
}

