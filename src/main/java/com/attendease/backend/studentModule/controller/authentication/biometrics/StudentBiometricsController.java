package com.attendease.backend.studentModule.controller.authentication.biometrics;

import com.attendease.backend.studentModule.dto.response.FacialEncodingResponse;
import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.studentModule.service.authentication.biometrics.StudentBiometricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/biometrics")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentBiometricsController {

    private final StudentBiometricsService studentBiometricsService;
    private final HttpHeaders httpHeaders;
    private final RestTemplate restTemplate;

    @Value("${extract.multiple.facial.encoding.endpoint}")
    private String extractMultipleFacialEncoding;

    @PostMapping(value = "/register-face-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> registerFacialDataFromImage(Authentication authentication, @RequestParam("images") List<MultipartFile> imageFile) {
        String authenticatedUserId = authentication.getName();
        String studentNumber = extractStudentNumberFromUserId(authenticatedUserId);

        if (imageFile == null || imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Image file cannot be null or empty");
        }

        if (imageFile.size() < 5) {
            return ResponseEntity.badRequest().body("At least 5 face images required for registration");
        }

        if (imageFile.size() > 5) {
            return ResponseEntity.badRequest().body("Maximum 5 images allowed");
        }

        log.info("Received image file for student {}: {} bytes", studentNumber, imageFile.size());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            for (int i = 0; i < imageFile.size(); i++) {
                MultipartFile imageFiles = imageFile.get(i);

                if (imageFiles.isEmpty()) {
                    return ResponseEntity.badRequest().body("Image " + (i + 1) + " is empty");
                }

                byte[] imageBytes = imageFiles.getBytes();
                ByteArrayResource contentsAsResource = new ByteArrayResource(imageBytes) {
                    @Override
                    public String getFilename() {
                        return imageFiles.getOriginalFilename();
                    }
                };

                body.add("files", contentsAsResource);

                log.debug("Added image {} to request: {} ({} bytes)",
                        i + 1, imageFiles.getOriginalFilename(), imageBytes.length);
            }
        } catch (IOException e) {
            log.error("Failed to read image files: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to process image files");
        }

        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);

        log.debug("Sending image to facial recognition service API for extraction");

        FacialEncodingResponse responseBody;
        try {
            ResponseEntity<FacialEncodingResponse> response = restTemplate.postForEntity(extractMultipleFacialEncoding, requestEntity, FacialEncodingResponse.class);
            responseBody = response.getBody();
        } catch (Exception e) {
            log.error("Failed to communicate with facial recognition service API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Face processing service is unavailable");
        }

        if (responseBody == null) {
            log.error("Facial recognition service returned null response body");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Face processing service returned invalid response");
        }

        if (!responseBody.isSuccess()) {
            String errorMsg = responseBody.getMessage() != null ? responseBody.getMessage() : (responseBody.getError() != null ? responseBody.getError() : "Unknown error from facial recognition service API");
            log.error("Facial recognition service API returned error: {}", errorMsg);
            return ResponseEntity.badRequest().body("Face processing failed: " + StringEscapeUtils.escapeHtml4(errorMsg));
        }

        List<Double> faceEncodingDoubles = responseBody.getFacialEncoding();
        if (faceEncodingDoubles == null || faceEncodingDoubles.isEmpty()) {
            log.error("Facial recognition service API returned null or empty face encoding data");
            return ResponseEntity.badRequest().body("Face processing failed: No face encoding data received");
        }

        log.info("Successfully received {} face encoding elements from facial recognition service API", faceEncodingDoubles.size());

        List<String> faceEncodingList = faceEncodingDoubles.stream().map(String::valueOf).collect(Collectors.toList());

        try {
            Students student = studentBiometricsService.getStudentByStudentNumber(studentNumber);
            String result = studentBiometricsService.registerNewFacialBiometrics(student, faceEncodingList);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            log.warn("Facial registration conflict for student {}: {}", studentNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Facial registration conflict for student " + studentNumber);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid facial registration request for student {}: {}", studentNumber, e.getMessage());
            return ResponseEntity.badRequest().body("Bad request: invalid parameters for facial registration request");
        } catch (Exception e) {
            log.error("Error registering facial data for student {}: {}", studentNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred during facial registration");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> getFacialStatus(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        String studentNumber = extractStudentNumberFromUserId(authenticatedUserId);

        Optional<BiometricData> biometricData = studentBiometricsService.getFacialStatus(studentNumber);
        return biometricData.map(data -> ResponseEntity.ok("Biometric status: " + data.getBiometricsStatus()))
                .orElseGet(() -> ResponseEntity.ok("No biometric data found for student " + studentNumber));
    }

    @DeleteMapping("/recalibrate")
    public ResponseEntity<String> recalibrateFacialData(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        String studentNumber = extractStudentNumberFromUserId(authenticatedUserId);

        studentBiometricsService.recalibrateFacialBiometrics(studentNumber);
        return ResponseEntity.ok("Facial data recalibrated successfully");
    }

    private String extractStudentNumberFromUserId(String userId) {
        return studentBiometricsService.getStudentNumberByUserId(userId).orElseThrow(() -> new IllegalArgumentException("No student profile found for authenticated user"));
    }
}