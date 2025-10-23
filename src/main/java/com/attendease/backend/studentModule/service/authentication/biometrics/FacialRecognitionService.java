package com.attendease.backend.studentModule.service.authentication.biometrics;

import com.attendease.backend.studentModule.dto.request.biometrics.FaceImageRequest;
import com.attendease.backend.studentModule.dto.request.biometrics.FaceVerificationRequest;
import com.attendease.backend.studentModule.dto.response.biometrics.FaceEncodingResponse;
import com.attendease.backend.studentModule.dto.response.biometrics.FaceVerificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacialRecognitionService {

    private final RestTemplate restTemplate;

    @Value("${extract.single.facial.encoding.endpoint}")
    private String extractSingleFaceEncoding;

    @Value("${facial.verification.endpoint}")
    private String verifyFacialAuthentication;

    /**
     * Extract facial encoding from base64 image
     */
    public FaceEncodingResponse extractFaceEncoding(String imageBase64) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            FaceImageRequest request = new FaceImageRequest();
            request.setImage_base64(imageBase64);

            HttpEntity<FaceImageRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<FaceEncodingResponse> response = restTemplate.postForEntity(
                    extractSingleFaceEncoding,
                    entity,
                    FaceEncodingResponse.class
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
     * verifies if two facial encodings are match
     */
    public FaceVerificationResponse verifyFace(List<String> uploadedEncoding, List<String> referenceEncoding) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            FaceVerificationRequest request = new FaceVerificationRequest();
            request.setUploaded_encoding(uploadedEncoding);
            request.setReference_encoding(referenceEncoding);

            HttpEntity<FaceVerificationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<FaceVerificationResponse> response = restTemplate.postForEntity(
                    verifyFacialAuthentication,
                    entity,
                    FaceVerificationResponse.class
            );

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
