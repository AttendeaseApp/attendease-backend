package com.attendease.backend.student.service.account.biometrics.management;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.biometrics.status.BiometricStatusResponse;

import java.util.Optional;

/**
 * Service layer for managing student facial biometric data.
 * <p>
 * Provides operations to:
 * <ul>
 *     <li>Retrieve the status of facial biometrics</li>
 *     <li>Recalibrate (delete) existing facial biometrics</li>
 *     <li>Resolve a student number from a user ID</li>
 * </ul>
 * </p>
 */
public interface AccountBiometricsManagementService {

    /**
     * Get facial biometric status without exposing encoding data
     * @param authenticatedUserId the authenticated user ID
     * @return BiometricStatusResponse with safe information only
     */
    Optional<BiometricStatusResponse> getFacialStatus(String authenticatedUserId);

    /**
     * Recalibrates (deletes) the facial biometric data for the given student number.
     *
     * @param authenticatedUserId the authenticated user id of the user
     * @throws IllegalArgumentException if no biometric data exists for the student
     */
    void deleteFacialData(String authenticatedUserId);
}
