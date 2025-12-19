package com.attendease.backend.domain.student.registration;

import jakarta.validation.constraints.*;
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

    private String firstName;
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String contactNumber;
    private String password;

    @NotBlank(message = "Student number is required")
    private String studentNumber;

    private String section;
}
