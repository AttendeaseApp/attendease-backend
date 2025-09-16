package com.attendease.backend.authentication.student.controller;

import com.attendease.backend.authentication.student.dto.request.LoginRequest;
import com.attendease.backend.authentication.student.dto.request.PasswordUpdateRequest;
import com.attendease.backend.authentication.student.service.StudentAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/student")
@RequiredArgsConstructor
public class StudentLoginController {
    private final StudentAuthenticationService authService;
    /**
     * Login a student using student number and password
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginStudent(@RequestBody LoginRequest request) {
        String token = authService.loginStudent(request.getStudentNumber(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    /**
     * Update student password
     */
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        String response = authService.updatePassword(request.getStudentNumber(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(response);
    }
}
