package com.attendease.backend.osaModule.controller.authentication;

import com.attendease.backend.osaModule.service.authentication.dto.OsaLoginRequest;
import com.attendease.backend.osaModule.service.authentication.service.OsaAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/osa")
public class OsaAuthenticationController {

    private final OsaAuthenticationService osaAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginOsa(@Valid @RequestBody OsaLoginRequest loginRequest) {
        String jwt = osaAuthenticationService.loginOsa(loginRequest.getEmail(), loginRequest.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("email", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }
}
