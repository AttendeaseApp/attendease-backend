package com.attendease.backend.studentModule.controller.authentication.biometrics;

import com.attendease.backend.studentModule.service.authentication.biometrics.BiometricsRegistrationService;
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
 * This controller exposes endpoints to allow authenticated students to upload their
 * facial images for biometric registration. The controller delegates the main
 * business logic to {@link BiometricsRegistrationService}.
 * </p>
 */
@RestController
@RequestMapping("/api/auth/biometrics")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class BiometricsRegistrationController {

    private final BiometricsRegistrationService biometricsRegistrationService;

    /**
     * Registers facial biometric data for the authenticated student.
     *
     * <p>The endpoint expects a multipart/form-data request with multiple face images.
     * The request is validated, processed through an external facial recognition service,
     * and saved in the database if successful.</p>
     *
     * @param authentication the {@link Authentication} object representing the logged-in student
     * @param images         a list of multipart image files containing the student's face
     * @return a {@link ResponseEntity} containing a success message or an error message if registration fails
     */
    @PostMapping(value = "/register-face-image", consumes = "multipart/form-data")
    public ResponseEntity<String> registerFacialDataFromImage(Authentication authentication, @RequestParam("images") List<MultipartFile> images) {
        String userId = authentication.getName();
        return biometricsRegistrationService.registerFacialBiometrics(userId, images);
    }
}
