package com.attendease.backend.studentModule.service.utils;

import com.attendease.backend.studentModule.dto.response.FacialEncodingResponse;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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
     * {@link FacialEncodingResponse}.</p>
     *
     * @param images the list of facial image files to process
     * @return a {@link FacialEncodingResponse} containing extracted facial encodings and metadata
     * @throws IOException if reading any of the image files fails
     * @throws IllegalStateException if communication with the external service fails
     */
    public FacialEncodingResponse extractFacialEncodings(List<MultipartFile> images) throws IOException {
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
            ResponseEntity<FacialEncodingResponse> response = restTemplate.postForEntity(extractMultipleFacialEncoding, requestEntity, FacialEncodingResponse.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to communicate with facial recognition service: {}", e.getMessage());
            throw new IllegalStateException("Face processing service unavailable");
        }
    }
}
