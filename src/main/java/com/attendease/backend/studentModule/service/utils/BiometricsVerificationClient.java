package com.attendease.backend.studentModule.service.utils;

import com.attendease.backend.domain.biometrics.Response.BiometricsServiceVerificationResponse;
import com.attendease.backend.domain.records.EventRegistration.EventRegistrationBiometricsVerification;
import com.attendease.backend.studentModule.dto.request.biometrics.FaceImageRequest;
import com.attendease.backend.studentModule.dto.request.biometrics.FaceVerificationRequest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricsVerificationClient {

    private final RestTemplate restTemplate;

    @Value("${extract.single.facial.encoding.endpoint}")
    private String extractSingleFaceEncoding;

    @Value("${facial.verification.endpoint}")
    private String verifyFacialAuthentication;

    /**
     * Extract facial encoding from base64 image
     */
    public EventRegistrationBiometricsVerification extractFaceEncoding(String imageBase64) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            FaceImageRequest request = new FaceImageRequest();
            request.setImage_base64(imageBase64);

            HttpEntity<FaceImageRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<EventRegistrationBiometricsVerification> response = restTemplate.postForEntity(extractSingleFaceEncoding, entity, EventRegistrationBiometricsVerification.class);

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
     * verifies if two facial encodings are match
     */
    public BiometricsServiceVerificationResponse verifyFace(List<Float> uploadedEncoding, List<Float> referenceEncoding) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            FaceVerificationRequest request = new FaceVerificationRequest();
            request.setUploaded_encoding(uploadedEncoding);
            request.setReference_encoding(referenceEncoding);

            HttpEntity<FaceVerificationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BiometricsServiceVerificationResponse> response = restTemplate.postForEntity(verifyFacialAuthentication, entity, BiometricsServiceVerificationResponse.class);

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
