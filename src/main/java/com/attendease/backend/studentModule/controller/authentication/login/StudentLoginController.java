package com.attendease.backend.studentModule.controller.authentication.login;

import com.attendease.backend.domain.students.Login.Request.LoginRequest;
import com.attendease.backend.domain.students.Login.Response.LoginResponse;
import com.attendease.backend.studentModule.service.authentication.StudentAuthenticationService;
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
    public ResponseEntity<LoginResponse> loginStudent(@RequestBody LoginRequest request) {
        LoginResponse response = authService.loginStudent(request.getStudentNumber(), request.getPassword());
        return ResponseEntity.ok(response);
    }

}
