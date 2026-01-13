package com.attendease.backend.student.service.utils;

import java.io.IOException;
import java.util.List;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.attendease.backend.domain.biometrics.Registration.Response.BiometricsRegistrationResponse;

/**
 * Client component responsible for communicating with an external facial recognition service.
 * <p>
 * This client handles the preparation of multipart/form-data requests containing facial images,
 * sends them to the configured external API, and returns the extracted facial encoding data.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BiometricsRegistrationClient {

    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;
    private final ObjectMapper objectMapper;

    /**
     * Endpoint URL for extracting multiple facial encodings from images.
     * This value is injected from application properties.
     */
    @Value("${extract.multiple.facial.encoding.endpoint}")
    private String extractMultipleFacialEncoding;

    /**
     * Sends a list of facial images to the external facial recognition service and retrieves
     * the corresponding facial encodings.
     *
     * <p>Each image is converted to a {@link ByteArrayResource} and added to a multipart request.
     * The request is sent using {@link RestTemplate}, and the response is mapped to
     * {@link BiometricsRegistrationResponse}.</p>
     *
     * @param images the list of facial image files to process
     * @return a {@link BiometricsRegistrationResponse} containing extracted facial encodings and metadata
     * @throws IOException if reading any of the image files fails
     * @throws IllegalStateException if communication with the external service fails
     */
    public BiometricsRegistrationResponse extractFacialEncodings(List<MultipartFile> images) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (MultipartFile file : images) {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("files", resource);
        }

        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);

        try {
            ResponseEntity<BiometricsRegistrationResponse> response = restTemplate.postForEntity(extractMultipleFacialEncoding, requestEntity, BiometricsRegistrationResponse.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            String errorMessage = extractErrorDetail(e.getResponseBodyAsString());
            log.error("Facial recognition service returned client error ({}): {}", e.getStatusCode(), errorMessage);
            throw new IllegalStateException(errorMessage);
        } catch (HttpServerErrorException e) {
            String errorMessage = extractErrorDetail(e.getResponseBodyAsString());
            log.error("Facial recognition service returned server error ({}): {}", e.getStatusCode(), errorMessage);
            throw new IllegalStateException("Face processing service error: " + errorMessage);
        } catch (Exception e) {
            log.error("Failed to communicate with facial recognition service: {}", e.getMessage(), e);
            throw new IllegalStateException("Face processing service unavailable: " + e.getMessage());
        }
    }


    private String extractErrorDetail(String responseBody) {
        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                if (jsonNode.has("detail")) {
                    return jsonNode.get("detail").asText();
                }
            }
            return responseBody;
        } catch (Exception e) {
            log.warn("Failed to parse error response, returning raw body: {}", e.getMessage());
            return responseBody;
        }
    }
}
