package com.attendease.backend.studentModule.service.utils;

import com.attendease.backend.domain.biometrics.Verification.Request.Base64ImageRequest;
import com.attendease.backend.domain.biometrics.Verification.Request.BiometricsVerificationRequest;
import com.attendease.backend.domain.biometrics.Verification.Response.BiometricsVerificationResponse;
import com.attendease.backend.domain.biometrics.Verification.Response.EventRegistrationBiometricsVerificationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Client service responsible for interacting with the external biometrics microservice.
 * <p>
 * Provides methods for:
 * <ul>
 *     <li>Extracting facial encodings from a Base64-encoded image</li>
 *     <li>Comparing a student's uploaded facial encoding against their registered encoding</li>
 * </ul>
 * </p>
 * This component abstracts away all HTTP communication and request/response mapping.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricsVerificationClient {

    private final RestTemplate restTemplate;

    /**
     * Endpoint used to extract a single facial encoding from a Base64 image.
     */
    @Value("${extract.single.facial.encoding.endpoint}")
    private String extractSingleFaceEncoding;

    /**
     * Endpoint used to perform facial encoding comparison (verification).
     */
    @Value("${facial.verification.endpoint}")
    private String verifyFacialAuthentication;

    /**
     * Sends a Base64-encoded image to the biometrics service for face detection and encoding extraction.
     *
     * @param imageBase64 Base64-encoded facial image
     * @return an {@link EventRegistrationBiometricsVerificationResponse} containing the extracted encoding and detection status
     *
     * @throws RuntimeException if:
     *         <ul>
     *             <li>The biometrics service returns an error</li>
     *             <li>No encoding is returned</li>
     *             <li>A network or serialization error occurs</li>
     *         </ul>
     */
    public EventRegistrationBiometricsVerificationResponse extractFaceEncoding(String imageBase64) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Base64ImageRequest request = new Base64ImageRequest();
            request.setImage_base64(imageBase64);

            HttpEntity<Base64ImageRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<EventRegistrationBiometricsVerificationResponse> response = restTemplate.postForEntity(
                extractSingleFaceEncoding,
                entity,
                EventRegistrationBiometricsVerificationResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }

            throw new RuntimeException("Failed to extract face encoding");
        } catch (Exception e) {
            log.error("Error extracting face encoding: {}", e.getMessage());
            throw new RuntimeException("Face detection failed: " + e.getMessage());
        }
    }

    /**
     * Sends two facial encodings to the biometrics service to determine whether they match.
     *
     * @param uploadedEncoding  the facial encoding derived from the user's uploaded image
     * @param referenceEncoding the pre-registered encoding stored for the student
     * @return a {@link BiometricsVerificationResponse} describing whether the two encodings match
     *
     * @throws RuntimeException if:
     *         <ul>
     *             <li>The verification service returns an error</li>
     *             <li>The response body is missing</li>
     *             <li>A network or serialization issue occurs</li>
     *         </ul>
     */
    public BiometricsVerificationResponse verifyFace(List<Float> uploadedEncoding, List<Float> referenceEncoding) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            BiometricsVerificationRequest request = new BiometricsVerificationRequest();
            request.setUploaded_encoding(uploadedEncoding);
            request.setReference_encoding(referenceEncoding);

            HttpEntity<BiometricsVerificationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BiometricsVerificationResponse> response = restTemplate.postForEntity(verifyFacialAuthentication, entity, BiometricsVerificationResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }

            throw new RuntimeException("Failed to verify face");
        } catch (Exception e) {
            log.error("Error verifying face: {}", e.getMessage());
            throw new RuntimeException("Face verification failed: " + e.getMessage());
        }
    }
}
