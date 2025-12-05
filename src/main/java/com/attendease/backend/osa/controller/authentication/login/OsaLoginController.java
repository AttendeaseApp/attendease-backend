package com.attendease.backend.osaModule.controller.authentication.login;

import com.attendease.backend.domain.users.OSA.Login.Request.OsaLoginRequest;
import com.attendease.backend.osaModule.service.authentication.login.OsaLoginService;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/osa")
public class OsaLoginController {

    private final OsaLoginService osaAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginOsa(@Valid @RequestBody OsaLoginRequest loginRequest) {
        String jwt = osaAuthenticationService.loginOsa(loginRequest.getEmail(), loginRequest.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("email", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }
}
