package com.attendease.backend.authentication.student.service;

import com.attendease.backend.authentication.student.dto.request.StudentRegistrationRequest;
import com.attendease.backend.authentication.student.repository.AuthenticationRepository;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.students.Sections;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.security.JwtTokenizationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Refactored StudentAuthenticationService that works with separate users and Students entities.
 * Uses composition domain instead of inheritance.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudentAuthenticationService {
    private final SectionsRepository sectionsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationRepository studentRepository;
    private final JwtTokenizationUtil jwtTokenizationUtil;

    /**
     * Registers a new student account using separate request DTO
     *
     * @param registrationRequest Contains all student registration data
     * @return Success message
     */
    public String registerNewStudentAccount(StudentRegistrationRequest registrationRequest) {
        validateRegistrationRequest(registrationRequest);

        if (studentRepository.existsByStudentNumber(registrationRequest.getStudentNumber())) {
            throw new IllegalArgumentException("Student number already exists.");
        }

        Users user = createUserFromRegistrationRequest(registrationRequest);

        Students student = createStudentFromRegistrationRequest(registrationRequest);
        student.setUser(user);

        studentRepository.saveUser(user);
        studentRepository.saveStudent(student);

        log.info("Registered new student account for studentNumber: {}", registrationRequest.getStudentNumber());

        return "Student account registered successfully.";
    }


    /**
     * Authenticates a student using student number and password
     *
     * @return Firebase custom token for authentication
     */
    public String loginStudent(String studentNumber, String password) {
        Users user = studentRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalArgumentException("Invalid email or password")).getUser();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtTokenizationUtil.generateToken(user.getUserId(), user.getEmail(), user.getUserType());
    }

    /**
     * Updates student password
     *
     * @param studentNumber The student number
     * @param oldPassword   Current password for verification
     * @param newPassword   New password to set
     * @return Success message
     */
    public String updatePassword(String studentNumber, String oldPassword, String newPassword) {
        if (studentNumber == null || studentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required.");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }

        validatePassword(newPassword);

        Students student = studentRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalArgumentException("Student not found."));

        Users user = student.getUser();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.updateUser(user);

        log.info("Password updated for studentNumber: {}", studentNumber);

        return "Password updated successfully.";
    }


    // helper methods

    private void validateRegistrationRequest(StudentRegistrationRequest request) {
        if (request.getStudentNumber() == null || request.getStudentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        validatePassword(request.getPassword());
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password cannot exceed 128 characters");
        }
        if (!password.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }
    }

    private Users createUserFromRegistrationRequest(StudentRegistrationRequest request) {
        Users user = new Users();
        user.setUserId(UUID.randomUUID().toString());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setContactNumber(request.getContactNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(UserType.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedBy(String.valueOf(UserType.SYSTEM));
        return user;
    }

    public Students createStudentFromRegistrationRequest(StudentRegistrationRequest request) {
        Students student = new Students();
        student.setStudentNumber(request.getStudentNumber());
        student.setSectionId(request.getSection());
        return student;
    }
}