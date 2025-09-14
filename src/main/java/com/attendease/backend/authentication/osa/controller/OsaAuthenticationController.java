package com.attendease.backend.authentication.osa.controller;

import com.attendease.backend.model.users.Users;
import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.enums.UserType;
import com.attendease.backend.authentication.osa.dto.OsaLoginRequest;
import com.attendease.backend.authentication.osa.service.OsaAuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth/osa")
@Slf4j
public class OsaAuthenticationController {

    private final OsaAuthenticationService osaAuthenticationService;

    public OsaAuthenticationController(OsaAuthenticationService osaAuthenticationService) {
        this.osaAuthenticationService = osaAuthenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerOsa(@RequestBody Users user) {
        user.setUserType(UserType.OSA);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return ResponseEntity.ok(osaAuthenticationService.registerNewOsaAccount(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginOsa(@Valid @RequestBody OsaLoginRequest loginRequest) {
        try {
            String jwt = osaAuthenticationService.loginOsa(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(jwt);
        } catch (IllegalArgumentException e) {
            log.warn("Login failed: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
