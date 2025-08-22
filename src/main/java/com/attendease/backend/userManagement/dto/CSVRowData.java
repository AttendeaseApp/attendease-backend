package com.attendease.backend.userManagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Data class for holding CSV row data during import operations.
 * Contains all the fields that can be imported from a CSV file for student creation.
 */
@Data
@NoArgsConstructor
public class CSVRowData {
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private String studentNumber;
    private String section;
    private String yearLevel;
    private String courseRefId;
    private Date birthdate;
    private String address;
    private String contactNumber;

    /**
     * Validates that all required fields are present
     * @return true if all required fields are non-null and non-empty
     */
    public boolean hasRequiredFields() {
        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                studentNumber != null && !studentNumber.trim().isEmpty() &&
                password != null && !password.trim().isEmpty();
    }

    /**
     * Gets a string representation of required fields for logging
     * @return comma-separated string of required field values
     */
    public String getRequiredFieldsString() {
        return String.format("firstName='%s', lastName='%s', studentNumber='%s', password='%s'",
                firstName, lastName, studentNumber, password != null ? "[PROTECTED]" : null);
    }
}