package com.attendease.backend.client.biometrics.utility;

import com.attendease.backend.exceptions.domain.Biometrics.InvalidBiometricImageException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility component for validating facial image upload requests for biometric registration.
 * <p>
 * This validator ensures that the list of images provided by the client meets the required
 * constraints:
 * <ul>
 *     <li>Must not be null or empty</li>
 *     <li>Must contain exactly 5 images</li>
 *     <li>No image should be empty</li>
 * </ul>
 * If the validation fails, an {@link InvalidBiometricImageException} is thrown.
 * </p>
 */
@Component
@Slf4j
public class BiometricImageRequestValidator {

    private static final int REQUIRED_IMAGES = 5;

    /**
     * Validates a list of facial image files for registration.
     *
     * <p>Validation rules:
     * <ul>
     *     <li>The list must not be null or empty.</li>
     *     <li>Exactly 5 images are required.</li>
     *     <li>None of the images can be empty.</li>
     * </ul>
     * </p>
     *
     * @param images the list of {@link MultipartFile} images to validate
     * @throws InvalidBiometricImageException if validation fails
     */
    public void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new InvalidBiometricImageException("Image file cannot be null or empty");
        }
        if (images.size() < REQUIRED_IMAGES) {
            throw new InvalidBiometricImageException("At least 5 face images required for registration");
        }
        if (images.size() > REQUIRED_IMAGES) {
            throw new InvalidBiometricImageException("Maximum 5 images allowed");
        }

        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).isEmpty()) {
                throw new InvalidBiometricImageException("Image " + (i + 1) + " is empty");
            }
        }
    }
}
