package com.attendease.backend.domain.students.Registration.Request;

import jakarta.validation.constraints.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student registration requests.
 * Contains all necessary information to create both User and Student entities.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-08-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$", message = "First name contains invalid characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$", message = "Last name contains invalid characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private Date birthdate;
    private String address;
    private String contactNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    private String password;

    @NotBlank(message = "Student number is required")
    private String studentNumber;

    private String section;

    private String courseRefId;

    private String clusterRefId;
}
