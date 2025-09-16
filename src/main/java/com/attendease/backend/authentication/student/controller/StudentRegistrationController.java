package com.attendease.backend.authentication.student.controller;

import com.attendease.backend.authentication.student.dto.request.StudentRegistrationRequest;
import com.attendease.backend.authentication.student.service.StudentAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/student")
@RequiredArgsConstructor
public class StudentRegistrationController {

    private final StudentAuthenticationService authService;

    /**
     * Register a new student account
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(@RequestBody StudentRegistrationRequest request) {
        String response = authService.registerNewStudentAccount(request);
        return ResponseEntity.ok(response);
    }
}
