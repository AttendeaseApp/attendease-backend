package com.attendease.backend.authentication.student.service;

import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.enums.UserType;
import com.attendease.backend.model.students.Students;
import com.attendease.backend.model.users.Users;
import com.attendease.backend.authentication.student.repository.StudentAuthenticationRepository;
import com.attendease.backend.authentication.student.dto.request.StudentRegistrationRequest;
import com.attendease.backend.authentication.student.dto.request.StudentLoginRequest;
import com.attendease.backend.userManagement.dto.UserWithStudentInfo;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Refactored StudentAuthenticationService that works with separate Users and Students entities.
 * Uses composition model instead of inheritance.
 */
@Service
@Slf4j
public class StudentAuthenticationService {

    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final PasswordEncoder passwordEncoder;
    private final StudentAuthenticationRepository studentRepository;

    public StudentAuthenticationService(Firestore firestore,
                                        FirebaseAuth firebaseAuth,
                                        PasswordEncoder passwordEncoder,
                                        StudentAuthenticationRepository studentRepository) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        this.passwordEncoder = passwordEncoder;
        this.studentRepository = studentRepository;
    }

    /**
     * Registers a new student account using separate request DTO
     * @param registrationRequest Contains all student registration data
     * @return Success message
     * @throws Exception if registration fails
     */
    public String registerNewStudentAccount(StudentRegistrationRequest registrationRequest) throws Exception {
        try {
            validateRegistrationRequest(registrationRequest);

            if (studentRepository.existsByStudentNumber(registrationRequest.getStudentNumber())) {
                throw new IllegalStateException("Student with number " + registrationRequest.getStudentNumber() + " already exists");
            }

            Users user = createUserFromRegistrationRequest(registrationRequest);
            Students student = createStudentFromRegistrationRequest(registrationRequest);

            UserWithStudentInfo savedInfo = studentRepository.saveWithTransaction(student, user);

            log.info("Student registered successfully: {} (User ID: {})",
                    savedInfo.getStudentNumber(), savedInfo.getUserId());
            return "Student registered successfully";

        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to register student: {}", e.getMessage(), e);
            throw new Exception("Failed to register student: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates a student using student number and password
     * @param loginRequest Contains student number and password
     * @return Firebase custom token for authentication
     */
    public String loginStudent(StudentLoginRequest loginRequest) {
        try {
            UserWithStudentInfo studentInfo = studentRepository.findByStudentNumberWithUserInfo(loginRequest.getStudentNumber()).orElseThrow(() -> {
                        log.warn("Login failed: Student with number {} not found", loginRequest.getStudentNumber());
                        return new IllegalArgumentException("Invalid student number or password");
            });

            Users user = studentInfo.getUser();
            Students student = studentInfo.getStudentInfo();

            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                log.warn("Login failed: Account {} is not active (Status: {})", user.getUserId(), user.getAccountStatus());
                throw new IllegalArgumentException("Account is not active. Please contact administration.");
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("Login failed: Invalid password for student {}", student.getStudentNumber());
                throw new IllegalArgumentException("Invalid student number or password");
            }

            String customToken = firebaseAuth.createCustomToken(user.getUserId());
            log.info("Login successful: Custom token generated for student {} (User ID: {})", student.getStudentNumber(), user.getUserId());
            return customToken;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (FirebaseAuthException e) {
            log.error("Failed to create custom token for student {}: {}",
                    loginRequest.getStudentNumber(), e.getMessage());
            throw new RuntimeException("Authentication failed, please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error during student login: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during login.");
        }
    }

    /**
     * Updates student password
     * @param studentNumber The student number
     * @param oldPassword Current password for verification
     * @param newPassword New password to set
     * @return Success message
     */
    public String updatePassword(String studentNumber, String oldPassword, String newPassword) {
        try {
            UserWithStudentInfo studentInfo = studentRepository.findByStudentNumberWithUserInfo(studentNumber).orElseThrow(() -> new IllegalArgumentException("Student number not found"));
            Users user = studentInfo.getUser();

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            validatePassword(newPassword);

            user.setPassword(passwordEncoder.encode(newPassword));
            studentRepository.updateUser(user);

            log.info("Password updated successfully for student {}", studentNumber);
            return "Password updated successfully";

        } catch (IllegalArgumentException e) {
            log.warn("Password update failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to update password for student {}: {}", studentNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to update password: " + e.getMessage(), e);
        }
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
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setBirthdate(request.getBirthdate());
        user.setAddress(request.getAddress());
        user.setContactNumber(request.getContactNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(UserType.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedBy(UserType.SYSTEM);
        return user;
    }

    private Students createStudentFromRegistrationRequest(StudentRegistrationRequest request) {
        Students student = new Students();
        student.setStudentNumber(request.getStudentNumber());
        student.setSection(request.getSection());
        student.setYearLevel(request.getYearLevel());

        if (request.getCourseRefId() != null) {
            try {
                student.setCourseRefId(firestore.document(request.getCourseRefId()));
            } catch (Exception e) {
                log.warn("Invalid courseRefId provided: {}", request.getCourseRefId());
                throw new IllegalArgumentException("Invalid course reference provided");
            }
        }

        if (request.getClusterRefId() != null) {
            try {
                student.setClusterRefId(firestore.document(request.getClusterRefId()));
            } catch (Exception e) {
                log.warn("Invalid clusterRefId provided: {}", request.getClusterRefId());
                throw new IllegalArgumentException("Invalid cluster reference provided");
            }
        }

        return student;
    }
}