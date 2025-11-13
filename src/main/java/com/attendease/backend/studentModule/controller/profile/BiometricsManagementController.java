package com.attendease.backend.studentModule.controller.profile;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.studentModule.service.authentication.biometrics.BiometricsRegistrationService;
import com.attendease.backend.studentModule.service.profile.BiometricsManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth/biometrics")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class BiometricsManagementController {

    private final BiometricsRegistrationService biometricsRegistrationService;
    private final BiometricsManagementService biometricsManagementService;

    @GetMapping("/status")
    public ResponseEntity<String> getFacialStatus(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        String studentNumber = extractStudentNumberFromUserId(authenticatedUserId);

        Optional<BiometricData> biometricData = biometricsManagementService.getFacialStatus(studentNumber);
        return biometricData.map(data -> ResponseEntity.ok("Biometric status: " + data.getBiometricsStatus()))
                .orElseGet(() -> ResponseEntity.ok("No biometric data found for student " + studentNumber));
    }

    @DeleteMapping("/recalibrate")
    public ResponseEntity<String> recalibrateFacialData(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        String studentNumber = extractStudentNumberFromUserId(authenticatedUserId);

        biometricsManagementService.recalibrateFacialBiometrics(studentNumber);
        return ResponseEntity.ok("Facial data recalibrated successfully");
    }

    private String extractStudentNumberFromUserId(String userId) {
        return biometricsRegistrationService.getStudentNumberByUserId(userId).orElseThrow(() -> new IllegalArgumentException("No student profile found for authenticated user"));
    }
}
