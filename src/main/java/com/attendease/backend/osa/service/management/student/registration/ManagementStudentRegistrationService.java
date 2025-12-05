package com.attendease.backend.osa.service.management.student.registration;

import com.attendease.backend.domain.students.Registration.Request.StudentRegistrationRequest;

/**
 * {@link ManagementStudentRegistrationService} is a service responsible for handling student account registrations.
 *
 * <p>Provides methods to create new student user account, including validation of user details, password encoding,
 * duplicate student number checks, and association with sections/courses. Ensures new account are set to
 * {@link com.attendease.backend.domain.enums.AccountStatus#ACTIVE} with {@link com.attendease.backend.domain.enums.UserType#STUDENT} type.
 * Links the student entity to a derived section and course if provided.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-06
 */
public interface ManagementStudentRegistrationService {

    /**
     * {@code registerNewStudentAccount} is used to register a new student account using the provided request DTO.
     * Performs validations on first name, last name, password, email, contact number, and student number.
     * Checks for existing student number to prevent duplicates. Encodes the password, creates and persists
     * linked {@link com.attendease.backend.domain.users.Users} and {@link com.attendease.backend.domain.students.Students} entities,
     * and associates with a section/course if specified (by ID or name).
     *
     * @param registrationRequest the {@link StudentRegistrationRequest} containing student details (first name, last name, password, email,
     * contact number, student number, optional section)
     * @return a success confirmation message (e.g., "Student account registered successfully.")
     * @throws IllegalArgumentException if validation fails (e.g., invalid fields, duplicate student number, invalid section/course)
     */
    String registerNewStudentAccount(StudentRegistrationRequest registrationRequest);
}
