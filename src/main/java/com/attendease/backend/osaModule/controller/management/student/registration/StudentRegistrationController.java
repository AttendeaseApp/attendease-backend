package com.attendease.backend.osaModule.controller.management.student.registration;

import com.attendease.backend.domain.students.Registration.Request.StudentRegistrationRequest;
import com.attendease.backend.osaModule.service.management.student.registration.StudentRegistrationService;
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

    private final StudentRegistrationService studentRegistrationService;

    /**
     * Register a new student account
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(@Valid @RequestBody StudentRegistrationRequest request) {
        String response = studentRegistrationService.registerNewStudentAccount(request);
        return ResponseEntity.ok(response);
    }
}
