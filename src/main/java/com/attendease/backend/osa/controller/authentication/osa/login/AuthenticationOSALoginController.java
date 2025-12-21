package com.attendease.backend.osa.controller.authentication.osa.login;

import com.attendease.backend.domain.user.account.osa.login.UserAccountOsaLoginRequest;

import com.attendease.backend.osa.service.authentication.osa.login.AuthenticationOSALoginService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * {@code AuthenticationOSALoginController} is a REST controller responsible for handling Office of Student Affairs (osa) login authentication requests.
 *
 * <p>This controller receives login requests from clients,
 * validates the input, and delegates authentication to
 * {@link AuthenticationOSALoginService}.</p>
 *
 *  @author jakematthewviado204@gmail.com
 *  @since 2025-Aug-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/osa")
public class AuthenticationOSALoginController {

    private final AuthenticationOSALoginService authenticationOSALoginService;

    /**
     * {@code loginOSA} is an endpoint used to authenticate the Office of Student Affairs (osa) user using email and password.
     *
     * @param loginRequest the login request containing email and password
     * @return a JWT token if authentication is successful
     *
     * @apiNote This endpoint is intended for osa-specific login only.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginOSA(@Valid @RequestBody UserAccountOsaLoginRequest loginRequest) {
        String jwt = authenticationOSALoginService.loginOSA(loginRequest.getEmail(), loginRequest.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("email", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }
}
