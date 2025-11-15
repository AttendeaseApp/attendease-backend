package com.attendease.backend.osaModule.controller.authentication;

import com.attendease.backend.domain.users.Users;
import com.attendease.backend.osaModule.service.authentication.service.OsaAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/osa")
@PreAuthorize("hasRole('OSA')")
public class OsaRegistrationController {

    private final OsaAuthenticationService osaAuthenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerOsa(@Valid @RequestBody Users user) {
        return ResponseEntity.ok(osaAuthenticationService.registerNewOsaAccount(user));
    }
}
