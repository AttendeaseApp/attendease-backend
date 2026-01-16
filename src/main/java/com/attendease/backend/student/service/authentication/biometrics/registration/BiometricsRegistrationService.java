package com.attendease.backend.student.service.authentication.biometrics.registration;

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
     * Registers facial biometric data for a student.
     *
     * @param authenticatedUserId the authenticated user ID
     * @param images the list of facial images to process if  encoding is invalid
     */
    void registerFacialBiometrics(String authenticatedUserId, List<MultipartFile> images);
}
