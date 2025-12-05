package com.attendease.backend.osa.controller.management.osa.registration;

import com.attendease.backend.domain.users.OSA.Registration.Request.OsaRegistrationRequest;
import com.attendease.backend.osa.service.management.osa.registration.OsaRegistrationService;
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

    private final OsaRegistrationService osaRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerOsa(@Valid @RequestBody OsaRegistrationRequest osaRegistrationRequest) {
        return ResponseEntity.ok(osaRegistrationService.registerNewOsaAccount(osaRegistrationRequest));
    }
}
