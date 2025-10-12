package com.attendease.backend.studentModule.controller.authentication.login;

import com.attendease.backend.studentModule.dto.request.LoginRequest;
import com.attendease.backend.studentModule.dto.request.PasswordUpdateRequest;
import com.attendease.backend.studentModule.dto.response.LoginResponse;
import com.attendease.backend.studentModule.service.StudentAuthenticationService;
import com.attendease.backend.studentModule.service.StudentBiometricsService;
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
    private final StudentBiometricsService biometricsService;
    /**
     * Login a student using student number and password
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginStudent(@RequestBody LoginRequest request) {
        LoginResponse response = authService.loginStudent(request.getStudentNumber(), request.getPassword());
        return ResponseEntity.ok(response);
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
