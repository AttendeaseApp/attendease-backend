package com.attendease.backend.osaModule.controller.authentication;

import com.attendease.backend.studentModule.dto.request.StudentRegistrationRequest;
import com.attendease.backend.studentModule.service.authentication.StudentAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class StudentRegistrationController {

    private final StudentAuthenticationService authService;

    /**
     * Register a new student account
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(@Valid @RequestBody StudentRegistrationRequest request) {
        String response = authService.registerNewStudentAccount(request);
        return ResponseEntity.ok(response);
    }
}
