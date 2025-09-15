package com.attendease.backend.authentication.student.controller;

import com.attendease.backend.authentication.student.dto.request.LoginRequest;
import com.attendease.backend.authentication.student.dto.request.PasswordUpdateRequest;
import com.attendease.backend.authentication.student.dto.request.StudentRegistrationRequest;
import com.attendease.backend.authentication.student.dto.response.PasswordChangeResponse;
import com.attendease.backend.authentication.student.service.StudentAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/student")
@RequiredArgsConstructor
public class StudentAuthenticationController {

    private final StudentAuthenticationService authService;

    /**
     * Register a new student account
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(@RequestBody StudentRegistrationRequest request) {
        try {
            String response = authService.registerNewStudentAccount(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Login a student using student number and password
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginStudent(@RequestBody LoginRequest request) {
        try {
            String token = authService.loginStudent(request.getStudentNumber(), request.getPassword());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    /**
     * Update student password
     */
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        try {
            String response = authService.updatePassword(request.getStudentNumber(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Password update failed: " + e.getMessage());
        }
    }
}
