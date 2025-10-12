package com.attendease.backend.studentModule.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * DTO for student registration requests.
 * Contains all necessary information to create both User and Student entities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRegistrationRequest {
    // user fields
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private Date birthdate;
    private String address;
    private String contactNumber;
    private String password;

    // student fields
    private String studentNumber;
    private String section;
    private String yearLevel;
    private String courseRefId;
    private String clusterRefId;

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
     * Gets a string representation of required fields for logging (protects password)
     * @return comma-separated string of required field values
     */
    public String getRequiredFieldsString() {
        return String.format("firstName='%s', lastName='%s', studentNumber='%s', password='%s'", firstName, lastName, studentNumber, password != null ? "[PROTECTED]" : null);
    }
}
