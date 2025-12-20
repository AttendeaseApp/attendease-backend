package com.attendease.backend.student.controller.authentication.login;

import com.attendease.backend.domain.student.login.LoginRequest;
import com.attendease.backend.student.service.authentication.login.AuthenticationLoginService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.attendease.backend.security.constants.SecurityConstants.JWT_TOKEN_HEADER;

@RestController
@RequestMapping("/api/auth/student")
@RequiredArgsConstructor
public class AuthenticationLoginController {

    private final AuthenticationLoginService authenticationLoginService;

    /**
     * login a student using student number and password
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        var result = authenticationLoginService.loginStudent(
                loginRequest.getStudentNumber(),
                loginRequest.getPassword()
        );

        if (result.token() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(JWT_TOKEN_HEADER, result.token());
            return ResponseEntity.ok().headers(headers).body(result.message());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result.message());
    }

}
