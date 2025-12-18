package com.attendease.backend.student.controller.profile;

import com.attendease.backend.student.service.profile.BiometricsManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing student facial biometrics.
 * <p>
 * Provides endpoints to check the status of a student's facial biometrics
 * and to recalibrate (delete) previously registered facial data.
 * Only user with the STUDENT role can access these endpoints.
 * </p>
 */
@RestController
@RequestMapping("/api/auth/biometrics")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class BiometricsManagementController {

    private final BiometricsManagementService biometricsManagementService;

    /**
     * GET endpoint to retrieve the current facial biometric status of the authenticated student.
     *
     * @param authentication the Spring Security authentication object containing the user's details
     * @return ResponseEntity with the biometric status message
     */
    @GetMapping("/status")
    public ResponseEntity<String> getFacialStatus(Authentication authentication) {
        String studentNumber = biometricsManagementService.getStudentNumberByUserId(authentication.getName());
        return biometricsManagementService.getFacialStatus(studentNumber).map(data -> ResponseEntity.ok("Biometric status: " + data.getBiometricsStatus())).orElseGet(() -> ResponseEntity.ok("No biometric data found for student " + studentNumber));
    }

    /**
     * DELETE endpoint to recalibrate (delete) the facial biometric data for the authenticated student.
     *
     * @param authentication the Spring Security authentication object containing the user's details
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/recalibrate")
    public ResponseEntity<String> recalibrateFacialData(Authentication authentication) {
        String studentNumber = biometricsManagementService.getStudentNumberByUserId(authentication.getName());
        biometricsManagementService.recalibrateFacialBiometrics(studentNumber);
        return ResponseEntity.ok("Facial data recalibrated successfully");
    }

}
