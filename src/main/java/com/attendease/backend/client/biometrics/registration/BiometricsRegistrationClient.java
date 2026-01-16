package com.attendease.backend.client.biometrics.registration;

import java.io.IOException;
import java.util.List;

import com.attendease.backend.exceptions.domain.Biometrics.FacialRecognitionServiceException;
import com.attendease.backend.exceptions.domain.Biometrics.Registration.BiometricProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.attendease.backend.domain.biometrics.Registration.Response.BiometricsRegistrationResponse;

/**
 * Client component responsible for communicating with an external facial recognition service.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BiometricsRegistrationClient {

    private final RestTemplate restTemplate;

    /**
     * Endpoint URL for extracting multiple facial encodings from images.
     * This value is injected from application properties.
     */
    @Value("${extract.multiple.facial.encoding.endpoint}")
    private String extractMultipleFacialEncoding;

    /**
     * Sends a list of facial images to the external facial recognition service.
     */
    public BiometricsRegistrationResponse extractFacialEncodings(List<MultipartFile> images) throws IOException {
        log.info("Preparing to send {} images to facial service", images.size());
        log.info("Target URL: {}", extractMultipleFacialEncoding);
        long totalSize = images.stream().mapToLong(MultipartFile::getSize).sum();
        log.info("Total upload size: {} MB ({} bytes)", totalSize / (1024 * 1024), totalSize);
        MultiValueMap<String, Object> body = buildMultipartBody(images);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = createRequestEntity(body);
        try {
            log.info("Sending request to facial service...");
            ResponseEntity<BiometricsRegistrationResponse> response = restTemplate.postForEntity(
                    extractMultipleFacialEncoding,
                    requestEntity,
                    BiometricsRegistrationResponse.class
            );
            BiometricsRegistrationResponse responseBody = response.getBody();
            if (responseBody == null) {
                log.error("Received null response body from facial recognition service");
                throw new BiometricProcessingException("Face processing service returned empty response");
            }
            log.info("Successfully received response from facial service - Status: {}", response.getStatusCode());
            return responseBody;
        } catch (HttpClientErrorException e) {
            log.error("Facial recognition service returned client error ({}): ", e.getStatusCode());
            log.error("CLIENT ERROR Target URL was: {}", extractMultipleFacialEncoding);
            throw new FacialRecognitionServiceException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Facial recognition service returned server error ({}): ", e.getStatusCode());
            log.error("SERVICE ERROR Target URL was: {}", extractMultipleFacialEncoding);
            throw new FacialRecognitionServiceException("Face processing service error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Failed to communicate with facial recognition service", e);
            log.error("COMMUNICATING ERROR Target URL was: {}", extractMultipleFacialEncoding);
            throw new FacialRecognitionServiceException("Face processing service unavailable: " + e.getMessage(), e);
        }
    }

    private MultiValueMap<String, Object> buildMultipartBody(List<MultipartFile> images) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            log.debug("Processing image {}: {} ({} KB)", i + 1, file.getOriginalFilename(), file.getSize() / 1024);
            try {
                ByteArrayResource resource = createByteArrayResource(file);
                body.add("files", resource);
            } catch (IOException e) {
                log.error("Failed to read image file at index {}: {}", i, file.getOriginalFilename(), e);
                throw new IOException("Failed to read image file: " + file.getOriginalFilename(), e);
            }
        }
        return body;
    }

    private ByteArrayResource createByteArrayResource(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }

    private HttpEntity<MultiValueMap<String, Object>> createRequestEntity(MultiValueMap<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new HttpEntity<>(body, headers);
    }
}