package com.attendease.backend.authentication.student.controller;

import com.attendease.backend.authentication.student.dto.request.PasswordChangeRequest;
import com.attendease.backend.authentication.student.dto.request.StudentLoginRequest;
import com.attendease.backend.authentication.student.dto.request.StudentRegistrationRequest;
import com.attendease.backend.authentication.student.dto.response.PasswordChangeResponse;
import com.attendease.backend.authentication.student.dto.response.StudentAuthenticationResponse;
import com.attendease.backend.authentication.student.dto.response.StudentRegistrationResponse;
import com.attendease.backend.authentication.student.service.StudentAuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/api/auth/student")
@Slf4j
public class StudentAuthenticationController {

    private final StudentAuthenticationService studentAuthenticationService;

    public StudentAuthenticationController(StudentAuthenticationService studentAuthenticationService) {
        this.studentAuthenticationService = studentAuthenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<StudentRegistrationResponse> register(@RequestBody StudentRegistrationRequest request) {
        try {
            if (!request.hasRequiredFields()) {
                log.warn("Invalid registration request: missing required fields");
                return ResponseEntity.badRequest().body(StudentRegistrationResponse.error("All required fields must be provided"));
            }

            String message = studentAuthenticationService.registerNewStudentAccount(request);

            StudentRegistrationResponse response = StudentRegistrationResponse.success(
                    request.getStudentNumber(),
                    request.getFirstName(),
                    request.getLastName(),
                    message
            );

            log.info("Student registration successful for student number: {}", request.getStudentNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            log.warn("Registration failed - student already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(StudentRegistrationResponse.error("Student already exists: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed - validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(StudentRegistrationResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StudentRegistrationResponse.error("An unexpected error occurred during registration"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<StudentAuthenticationResponse> loginStudent(@RequestBody StudentLoginRequest loginRequest) {
        try {
            if (!loginRequest.isValid()) {
                log.warn("Invalid login request: missing required fields");
                return ResponseEntity.badRequest().body(StudentAuthenticationResponse.error("All fields are required"));
            }

            String token = studentAuthenticationService.loginStudent(loginRequest);

            StudentAuthenticationResponse response = new StudentAuthenticationResponse();
            response.setToken(token);
            response.setMessage("Authentication successful");

            log.info("Student login successful for student number: {}", loginRequest.getStudentNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(StudentAuthenticationResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Runtime error during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StudentAuthenticationResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StudentAuthenticationResponse.error("An unexpected error occurred during login"));
        }
    }

    /**
     * Changes a student's password
     *
     * @param passwordChangeRequest The password change request containing student number, old password, and new password
     * @return ResponseEntity with success message or error details
     */
    @PutMapping("/change-password")
    public ResponseEntity<PasswordChangeResponse> changePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            if (!passwordChangeRequest.isValid()) {
                log.warn("Invalid password change request: missing required fields");
                return ResponseEntity.badRequest().body(new PasswordChangeResponse(false, "All fields are required", null));
            }

            String result = studentAuthenticationService.updatePassword(
                    passwordChangeRequest.getStudentNumber(),
                    passwordChangeRequest.getOldPassword(),
                    passwordChangeRequest.getNewPassword()
            );

            log.info("Password change successful for student: {}", passwordChangeRequest.getStudentNumber());
            return ResponseEntity.ok(new PasswordChangeResponse(true, result, null));

        } catch (IllegalArgumentException e) {
            log.warn("Password change failed for student {}: {}", passwordChangeRequest.getStudentNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(new PasswordChangeResponse(false, e.getMessage(), "VALIDATION_ERROR"));
        } catch (RuntimeException e) {
            log.error("Runtime error during password change for student {}: {}", passwordChangeRequest.getStudentNumber(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PasswordChangeResponse(false, "Internal server error occurred", "SERVER_ERROR"));
        } catch (Exception e) {
            log.error("Unexpected error during password change for student {}: {}", passwordChangeRequest.getStudentNumber(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PasswordChangeResponse(false, "An unexpected error occurred", "UNKNOWN_ERROR"));
        }
    }
}
