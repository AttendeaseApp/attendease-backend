package com.attendease.backend.student.service.account.biometrics.management;

import com.attendease.backend.domain.biometrics.BiometricData;

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
     * Retrieves the facial biometric status for a given student number.
     *
     * @param authenticatedUserId the authenticated user id of the user
     * @return Optional containing BiometricData if present, otherwise empty
     */
    Optional<BiometricData> getFacialStatus(String authenticatedUserId);

    /**
     * Recalibrates (deletes) the facial biometric data for the given student number.
     *
     * @param authenticatedUserId the authenticated user id of the user
     * @throws IllegalArgumentException if no biometric data exists for the student
     */
    void deleteFacialData(String authenticatedUserId);
}
