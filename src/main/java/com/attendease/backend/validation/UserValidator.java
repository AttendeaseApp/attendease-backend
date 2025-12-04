package com.attendease.backend.validation;

import com.attendease.backend.validation.rules.UserValidationRules;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public static void validateFirstName(String value, String fieldName) {
        if (value == null || !value.matches(UserValidationRules.FIRSTNAME_FORMAT_REGEX)) {
            throw new IllegalArgumentException(
                    fieldName + " is invalid. Only letters, spaces, apostrophes, and hyphens are allowed, and it cannot be empty."
            );
        }
    }

    public static void validateLastName(String value, String fieldName) {
        if (value == null || !value.matches(UserValidationRules.LASTNAME_FORMAT_REGEX)) {
            throw new IllegalArgumentException(
                    fieldName + " is invalid. Only letters, spaces, apostrophes, and hyphens are allowed, and it cannot be empty."
            );
        }
    }


    public static void validatePassword(String value) {
        if (value == null || !value.matches(UserValidationRules.PASSWORD_FORMAT_REGEX)) {
            throw new IllegalArgumentException(
                    "Password must contain uppercase, lowercase, digit, special character, and be 8-128 characters long"
            );
        }
    }

    public static void validateContactNumber(String value) {
        if (value != null && !value.matches(UserValidationRules.CONTACT_NUMBER_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Invalid contact number format");
        }
    }

    public static void validateEmail(String value) {
        if (value != null && !value.matches(UserValidationRules.EMAIL_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public static void validateStudentNumber(String value) {
        if (value == null || !value.matches(UserValidationRules.STUDENT_NUMBER_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Invalid student number format (expected CTyy-xxxx)");
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
        if (fullIdentifier == null || !fullIdentifier.matches(UserValidationRules.SECTION_NAME_FORMAT_REGEX)) {
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
        if (!name.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException(
                    "Invalid cluster name '" + name + "'. Only uppercase letters, digits, and dashes are allowed, without spaces."
            );
        }
    }
}

