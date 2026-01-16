package com.attendease.backend.domain.biometrics.Verification.Request;

import com.attendease.backend.client.biometrics.verification.BiometricsVerificationClient;
import lombok.Data;

/**
 * Represents a request containing a Base64-encoded image for facial biometrics processing.
 *
 * <p>
 * This object is primarily used when interacting with the biometrics microservice
 * to extract facial encodings from an image.
 * </p>
 *
 * <p>
 * This class is used in {@link BiometricsVerificationClient#extractFaceEncoding(String)}
 * when sending a request to the biometrics service.
 * </p>
 */
@Data
public class Base64ImageRequest {

    private String image_base64;
}
