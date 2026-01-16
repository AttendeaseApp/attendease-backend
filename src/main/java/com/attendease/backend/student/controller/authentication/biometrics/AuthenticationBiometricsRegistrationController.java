package com.attendease.backend.student.controller.authentication.biometrics;

import com.attendease.backend.student.service.authentication.biometrics.registration.BiometricsRegistrationService;
import com.attendease.backend.student.service.authentication.biometrics.registration.impl.BiometricsRegistrationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller responsible for handling student facial biometric registration.
 * <p>
 * This controller exposes endpoints to allow authenticated student to upload their
 * facial images for biometric registration. The controller delegates the main
 * business logic to {@link BiometricsRegistrationServiceImpl}.
 * </p>
 */
@RestController
@RequestMapping("/api/auth/biometrics")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AuthenticationBiometricsRegistrationController {

    private final BiometricsRegistrationService biometricsRegistrationService;

    /**
     * Registers facial biometric data for the authenticated student.
     *
     * <p>The endpoint expects a multipart/form-data request with multiple face images.
     * The request is validated, processed through an external facial recognition service,
     * and saved in the database if successful.</p>
     *
     * @param authentication the {@link Authentication} object representing the logged-in student
     * @param images a list of multipart image files containing the student's face
     * @return ResponseEntity with success message
     */
    @PostMapping(value = "/register-face-image", consumes = "multipart/form-data")
    public ResponseEntity<String> registerFacialDataFromImage(Authentication authentication, @RequestParam("images") List<MultipartFile> images) {
        String authenticatedUserId = authentication.getName();
        log.info("Attempting to register facial biometric data for user {}", authenticatedUserId);
        biometricsRegistrationService.registerFacialBiometrics(authenticatedUserId, images);
        return ResponseEntity.ok("Your facial biometrics registered successfully");
    }
}