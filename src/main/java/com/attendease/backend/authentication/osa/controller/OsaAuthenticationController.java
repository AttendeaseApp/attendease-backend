package com.attendease.backend.authentication.osa.controller;

import com.attendease.backend.domain.users.Users;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.authentication.osa.dto.OsaLoginRequest;
import com.attendease.backend.authentication.osa.service.OsaAuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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
        String jwt = osaAuthenticationService.loginOsa(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(jwt);
    }
}
