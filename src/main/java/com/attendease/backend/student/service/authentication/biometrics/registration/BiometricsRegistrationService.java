package com.attendease.backend.student.service.authentication.biometrics.registration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * BiometricsRegistrationServiceImpl is responsible for registering and managing facial biometric data for student.
 * <p>
 * This service handles validation of uploaded images, communication with an external
 * facial recognition API, and persistence of biometric data to the database.
 * </p>
 */
public interface BiometricsRegistrationService {

    /**
     * Registers facial biometrics for a student associated with the given user ID.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Validate uploaded image files.</li>
     *     <li>Retrieve the student number associated with the user ID.</li>
     *     <li>Call an external facial recognition service to extract facial encodings.</li>
     *     <li>Validate the API response and encoding data.</li>
     *     <li>Persist the biometric data to the database.</li>
     * </ol>
     *
     * @param authenticatedUserId the ID of the user whose facial biometrics are being registered
     * @param images a list of uploaded image files containing the student's face
     * @return a {@link ResponseEntity} with success or error message
     */
    ResponseEntity<String> registerFacialBiometrics(String authenticatedUserId, List<MultipartFile> images);
}
