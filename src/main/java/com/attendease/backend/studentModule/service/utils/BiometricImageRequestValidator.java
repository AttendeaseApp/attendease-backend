package com.attendease.backend.studentModule.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.List;

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
 * If the validation fails, a {@link ResponseEntity} with a corresponding HTTP 400 (Bad Request)
 * response is returned. If validation passes, {@code null} is returned.
 * </p>
 */
@Component
@Slf4j
public class BiometricImageRequestValidator {

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
     * @return a {@link ResponseEntity} containing an error message if validation fails, or {@code null} if validation succeeds
     */
    public ResponseEntity<String> validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest().body("Image file cannot be null or empty");
        }
        if (images.size() < 5) {
            return ResponseEntity.badRequest().body("At least 5 face images required for registration");
        }
        if (images.size() > 5) {
            return ResponseEntity.badRequest().body("Maximum 5 images allowed");
        }

        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).isEmpty()) {
                return ResponseEntity.badRequest().body("Image " + (i + 1) + " is empty");
            }
        }

        return null;
    }
}

