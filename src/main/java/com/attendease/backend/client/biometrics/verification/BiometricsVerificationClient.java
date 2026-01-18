package com.attendease.backend.client.biometrics.verification;

import com.attendease.backend.domain.biometrics.Verification.Response.BiometricsVerificationResponse;
import com.attendease.backend.domain.biometrics.Verification.Response.EventRegistrationBiometricsVerificationResponse;
import com.attendease.backend.domain.biometrics.Verification.Request.BiometricsVerificationRequest;
import com.attendease.backend.exceptions.domain.Biometrics.FacialRecognitionServiceException;
import com.attendease.backend.exceptions.domain.Biometrics.Registration.BiometricProcessingException;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Client service responsible for interacting with the external biometrics microservice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricsVerificationClient {

    private final RestTemplate restTemplate;

    /**
     * Endpoint used to extract a single facial encoding from an uploaded image file.
     * Maps to Python endpoint: POST /extract-face-encoding
     */
    @Value("${extract.single.facial.encoding.endpoint}")
    private String extractSingleFaceEncoding;

    /**
     * Endpoint used to perform facial encoding comparison (verification).
     * Maps to Python endpoint: POST /verify-face
     */
    @Value("${facial.verification.endpoint}")
    private String verifyFacialAuthentication;

    /**
     * Extracts facial encoding from an uploaded image file.
     *
     * @param imageFile the MultipartFile containing the facial image
     * @return EventRegistrationBiometricsVerificationResponse containing the facial encoding
     * @throws BiometricProcessingException if face detection fails
     * @throws FacialRecognitionServiceException if the external service is unavailable
     */
    public EventRegistrationBiometricsVerificationResponse extractFaceEncoding(MultipartFile imageFile) {
        try {
            log.info("Extracting face encoding from uploaded file: {}", imageFile.getOriginalFilename());
            log.debug("Target URL extractSingleFaceEncoding: {}", extractSingleFaceEncoding);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<EventRegistrationBiometricsVerificationResponse> response = restTemplate.postForEntity(
                    extractSingleFaceEncoding,
                    entity,
                    EventRegistrationBiometricsVerificationResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                EventRegistrationBiometricsVerificationResponse responseBody = response.getBody();
                log.info("Successfully extracted face encoding. Quality: {}",
                        responseBody.getMetadata() != null ? responseBody.getMetadata().get("average_quality") : "N/A");
                return responseBody;
            }

            log.error("Failed to extract face encoding - Status: {}", response.getStatusCode());
            throw new BiometricProcessingException("Failed to extract face encoding from image");

        } catch (IOException e) {
            log.error("Failed to read image file: {}", e.getMessage());
            throw new BiometricProcessingException("Failed to read uploaded image file");

        } catch (HttpClientErrorException e) {
            log.error("Client error while extracting face encoding: Status {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            String errorMessage = parseErrorMessage(e.getResponseBodyAsString());
            throw new BiometricProcessingException(errorMessage != null ? errorMessage :
                    "Invalid image format or face not detected in the image");

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

            ResponseEntity<BiometricsVerificationResponse> response = restTemplate.postForEntity(
                    verifyFacialAuthentication,
                    entity,
                    BiometricsVerificationResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                BiometricsVerificationResponse responseBody = response.getBody();
                log.info("Face verification completed - Match: {}, Distance: {}, Confidence: {}",
                        responseBody.getIs_face_matched(),
                        responseBody.getFace_distance(),
                        responseBody.getConfidence());
                return responseBody;
            }

            log.error("Failed to verify face - Status: {}", response.getStatusCode());
            throw new BiometricProcessingException("Failed to complete facial verification");

        } catch (HttpClientErrorException e) {
            log.error("Client error during face verification: Status {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BiometricProcessingException("Invalid facial encoding data provided for verification");

        } catch (HttpServerErrorException e) {
            log.error("Server error from facial verification service: Status {}", e.getStatusCode());
            throw new FacialRecognitionServiceException(
                    "Facial verification service is currently unavailable. Please try again later.");

        } catch (ResourceAccessException e) {
            log.error("Cannot connect to facial verification service: {}", e.getMessage());
            throw new FacialRecognitionServiceException(
                    "Cannot connect to facial verification service. Please try again later.");

        } catch (BiometricProcessingException | FacialRecognitionServiceException e) {
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error verifying face at URL: {}", verifyFacialAuthentication, e);
            throw new BiometricProcessingException("Face verification failed: " + e.getMessage());
        }
    }


    private String parseErrorMessage(String responseBody) {
        try {
            if (responseBody != null && responseBody.contains("detail")) {
                int startIdx = responseBody.indexOf("\"detail\":");
                if (startIdx != -1) {
                    int valueStart = responseBody.indexOf("\"", startIdx + 10);
                    int valueEnd = responseBody.indexOf("\"", valueStart + 1);
                    if (valueStart != -1 && valueEnd != -1) {
                        return responseBody.substring(valueStart + 1, valueEnd);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse error message from response body", e);
        }
        return null;
    }
}