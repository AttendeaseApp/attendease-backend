package com.attendease.backend.studentModule.service.authentication;

import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.students.Login.Response.LoginResponse;
import com.attendease.backend.domain.students.Registration.Request.StudentRegistrationRequest;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.security.JwtTokenizationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Refactored StudentAuthenticationService that works with separate users and Students entities.
 * Uses composition domain instead of inheritance.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudentAuthenticationService {

    private final UserRepository userRepository;
    private final SectionsRepository sectionsRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final JwtTokenizationUtil jwtTokenizationUtil;
    private final BiometricsRepository biometricsRepository;

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

        userRepository.save(user);
        studentRepository.save(student);

        log.info("Registered new student account for studentNumber: {}", registrationRequest.getStudentNumber());

        return "Student account registered successfully.";
    }


    public LoginResponse loginStudent(String studentNumber, String password) {
        Students student = studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student number or password"));

        Users user = student.getUser();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid student number or password");
        }

        Optional<BiometricData> biometricData = biometricsRepository.findByStudentNumber(studentNumber);
        boolean requiresFacialRegistration = biometricData.isEmpty();

        String token = jwtTokenizationUtil.generateToken(user.getUserId(), user.getEmail(), user.getUserType());

        log.info("Student login successful. StudentNumber: {}, Requires Facial Registration: {}", studentNumber, requiresFacialRegistration);

        return LoginResponse.builder()
                .token(token)
                .requiresFacialRegistration(requiresFacialRegistration)
                .message(requiresFacialRegistration ? "Login successful. Please complete facial registration." : "Login successful.").studentNumber(studentNumber).build();
    }

    /**
     * Updates student password
     *
     * @param id The student number
     * @param oldPassword   Current password for verification
     * @param newPassword   New password to set
     * @return Success message
     */
    public String updatePassword(String id, String oldPassword, String newPassword) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required.");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }

        validatePassword(newPassword);

        Students student = studentRepository.findByUserId(id).orElseThrow(() -> new IllegalArgumentException("Student not found."));
        Users user = student.getUser();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

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
        if (!Pattern.compile("[A-Za-z]").matcher(password).find()) {
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