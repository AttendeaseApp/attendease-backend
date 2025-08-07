package com.attendease.attendease_backend.controller.authentication.osa;

import com.attendease.attendease_backend.data.user.User;
import com.attendease.attendease_backend.data.enums.AccountStatus;
import com.attendease.attendease_backend.data.enums.UserType;
import com.attendease.attendease_backend.payload.request.OsaLoginRequest;
import com.attendease.attendease_backend.services.authentication.osa.impl.OsaAuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("v1/api/auth/osa")
@Slf4j
public class OsaAuthenticationController {

    private final OsaAuthenticationService osaAuthenticationService;

    public OsaAuthenticationController(OsaAuthenticationService osaAuthenticationService) {
        this.osaAuthenticationService = osaAuthenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerOsa(@RequestBody User user) {
        user.setUserType(UserType.OSA);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return ResponseEntity.ok(osaAuthenticationService.registerNewOsaAccount(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginOsa(@Valid @RequestBody OsaLoginRequest loginRequest) {
        try {
            User tempStudent = new User();
            tempStudent.setEmail(loginRequest.getEmail());
            tempStudent.setPassword(loginRequest.getPassword());

            String customToken = osaAuthenticationService.loginOsa(tempStudent);
            return new ResponseEntity<>(customToken, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Login attempt failed: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Internal server error during student login service: {}", e.getMessage());
            return new ResponseEntity<>("Authentication failed due to server error.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            log.error("Unexpected error during OSA login service: {}", e.getMessage());
            return new ResponseEntity<>("An unexpected error occurred during OSA login service.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
