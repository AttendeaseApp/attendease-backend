package com.attendease.backend.client.biometrics.verification;

import com.attendease.backend.domain.biometrics.Verification.Request.Base64ImageRequest;
import com.attendease.backend.domain.biometrics.Verification.Request.BiometricsVerificationRequest;
import com.attendease.backend.domain.biometrics.Verification.Response.BiometricsVerificationResponse;
import com.attendease.backend.domain.biometrics.Verification.Response.EventRegistrationBiometricsVerificationResponse;
import com.attendease.backend.exceptions.domain.Biometrics.FacialRecognitionServiceException;
import java.util.List;

import com.attendease.backend.exceptions.domain.Biometrics.Registration.BiometricProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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
     *
     * @param imageBase64 the Base64 encoded image
     * @return EventRegistrationBiometricsVerificationResponse containing the facial encoding
     * @throws BiometricProcessingException if face detection fails
     * @throws FacialRecognitionServiceException if the external service is unavailable
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
            throw new BiometricProcessingException("Failed to extract face encoding from image");

        } catch (HttpClientErrorException e) {
            log.error("Client error while extracting face encoding: Status {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BiometricProcessingException("Invalid image format or face not detected in the image");

        } catch (HttpServerErrorException e) {
            log.error("Server error from facial recognition service: Status {}", e.getStatusCode());
            throw new FacialRecognitionServiceException(
                    "Facial recognition service is currently unavailable. Please try again later.");

        } catch (ResourceAccessException e) {
            log.error("Cannot connect to facial recognition service: {}", e.getMessage());
            throw new FacialRecognitionServiceException(
                    "Cannot connect to facial recognition service. Please try again later.");

        } catch (BiometricProcessingException | FacialRecognitionServiceException e) {
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error extracting face encoding from URL: {}", extractSingleFaceEncoding, e);
            throw new BiometricProcessingException("Face detection failed: " + e.getMessage());
        }
    }

    /**
     * Verifies two facial encodings to determine whether they match.
     *
     * @param uploadedEncoding the facial encoding from the uploaded image
     * @param referenceEncoding the reference facial encoding from the database
     * @return BiometricsVerificationResponse containing verification result
     * @throws BiometricProcessingException if verification fails
     * @throws FacialRecognitionServiceException if the external service is unavailable
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
            throw new BiometricProcessingException("Failed to complete facial verification");

        } catch (HttpClientErrorException e) {
            log.error("Client error during face verification: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BiometricProcessingException("Invalid facial encoding data provided for verification");

        } catch (HttpServerErrorException e) {
            log.error("Server error from facial verification service: Status {}", e.getStatusCode());
            throw new FacialRecognitionServiceException("Facial verification service is currently unavailable. Please try again later.");

        } catch (ResourceAccessException e) {
            log.error("Cannot connect to facial verification service: {}", e.getMessage());
            throw new FacialRecognitionServiceException("Cannot connect to facial verification service. Please try again later.");

        } catch (BiometricProcessingException | FacialRecognitionServiceException e) {
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error verifying face at URL: {}", verifyFacialAuthentication, e);
            throw new BiometricProcessingException("Face verification failed: " + e.getMessage());
        }
    }
}
