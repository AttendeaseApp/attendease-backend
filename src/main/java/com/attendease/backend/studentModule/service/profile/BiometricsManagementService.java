package com.attendease.backend.studentModule.service.profile;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.studentModule.service.authentication.biometrics.BiometricsRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
@Service
@Slf4j
@RequiredArgsConstructor
public class BiometricsManagementService {

    private final BiometricsRepository biometricsRepository;
    private final BiometricsRegistrationService biometricsRegistrationService;

    /**
     * Retrieves the facial biometric status for a given student number.
     *
     * @param studentNumber the unique student number
     * @return Optional containing BiometricData if present, otherwise empty
     */
    public Optional<BiometricData> getFacialStatus(String studentNumber) {
        return biometricsRepository.findByStudentNumber(studentNumber);
    }

    /**
     * Recalibrates (deletes) the facial biometric data for the given student number.
     *
     * @param studentNumber the unique student number
     * @throws IllegalArgumentException if no biometric data exists for the student
     */
    public void recalibrateFacialBiometrics(String studentNumber) {
        Optional<BiometricData> existing = biometricsRepository.findByStudentNumber(studentNumber);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("No facial biometric data found for this student.");
        }

        biometricsRepository.deleteByStudentNumber(studentNumber);

        log.info("Facial biometric data deleted for student: {}", studentNumber);
    }

    public String getStudentNumberByUserId(String userId) {
        return biometricsRegistrationService.getStudentNumberByUserId(userId).orElseThrow(() -> new IllegalArgumentException("No student profile found for authenticated user"));
    }
}
