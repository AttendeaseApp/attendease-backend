package com.attendease.backend.student.controller.account.biometrics.management;

import com.attendease.backend.domain.biometrics.status.BiometricStatusResponse;
import com.attendease.backend.student.service.account.biometrics.management.AccountBiometricsManagementService;
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
@RequestMapping("/api/manage/biometrics")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AccountBiometricsManagementController {

    private final AccountBiometricsManagementService accountBiometricsManagementService;

    /**
     * GET endpoint to retrieve the current facial biometric status of the authenticated student.
     * This endpoint will NOT expose facial encoding data for security reasons.
     *
     * @param authentication the Spring Security authentication object containing the user's details
     * @return ResponseEntity with the biometric status
     */
    @GetMapping("/status")
    public ResponseEntity<BiometricStatusResponse> getFacialStatus(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        return accountBiometricsManagementService.getFacialStatus(authenticatedUserId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE endpoint to recalibrate (delete) the facial biometric data for the authenticated student.
     *
     * @param authentication the Spring Security authentication object containing the user's details
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFacialData(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        accountBiometricsManagementService.deleteFacialData(authenticatedUserId);
        return ResponseEntity.ok("Your facial data has been deleted from our database successfully");
    }

}
