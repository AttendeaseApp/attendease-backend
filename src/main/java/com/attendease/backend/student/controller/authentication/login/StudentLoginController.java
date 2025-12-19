package com.attendease.backend.student.controller.authentication.login;

import com.attendease.backend.domain.student.login.LoginRequest;
import com.attendease.backend.domain.student.login.LoginResponse;
import com.attendease.backend.student.service.authentication.login.AuthenticationLoginService;

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

    private final AuthenticationLoginService authService;

    /**
     * login a student using student number and password
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginStudent(@RequestBody LoginRequest request) {
        LoginResponse response = authService.loginStudent(request.getStudentNumber(), request.getPassword());
        return ResponseEntity.ok(response);
    }

}
