package com.attendease.backend.student.service.utils;

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
     * Extracts facial encoding from a Base64 image.
     */
    public EventRegistrationBiometricsVerificationResponse extractFaceEncoding(String imageBase64) {
        try {
            log.info("Extracting face encoding from Base64 image");
            log.debug("Target URL extractSingleFaceEncoding: {}", extractSingleFaceEncoding);

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
                log.info("Successfully extracted face encoding");
                return response.getBody();
            }

            log.error("Failed to extract face encoding - Status: {}", response.getStatusCode());
            throw new RuntimeException("Failed to extract face encoding");
        } catch (Exception e) {
            log.error("Error extracting face encoding from URL: {}", extractSingleFaceEncoding, e);
            throw new RuntimeException("Face detection failed: " + e.getMessage());
        }
    }

    /**
     * Verifies two facial encodings to the biometrics service to determine whether they match.
     */
    public BiometricsVerificationResponse verifyFace(List<Float> uploadedEncoding, List<Float> referenceEncoding) {
        try {
            log.info("Verifying face encodings");
            log.debug("Target URL verifyFacialAuthentication: {}", verifyFacialAuthentication);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            BiometricsVerificationRequest request = new BiometricsVerificationRequest();
            request.setUploaded_encoding(uploadedEncoding);
            request.setReference_encoding(referenceEncoding);

            HttpEntity<BiometricsVerificationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BiometricsVerificationResponse> response = restTemplate.postForEntity(verifyFacialAuthentication, entity, BiometricsVerificationResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Face verification completed - Match: {}", response.getBody().getIs_face_matched());
                return response.getBody();
            }

            log.error("Failed to verify face - Status: {}", response.getStatusCode());
            throw new RuntimeException("Failed to verify face");
        } catch (Exception e) {
            log.error("Error verifying face at URL: {}", verifyFacialAuthentication, e);
            throw new RuntimeException("Face verification failed: " + e.getMessage());
        }
    }
}
