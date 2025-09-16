package com.attendease.backend.authentication.student.controller;

import com.attendease.backend.authentication.student.dto.request.FacialRegistrationRequest;
import com.attendease.backend.authentication.student.dto.response.FacialEncodingResponse;
import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.authentication.student.service.StudentBiometricsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/biometrics")
@Slf4j
@PreAuthorize("hasRole('STUDENT')")
public class StudentBiometricsController {

    private final StudentBiometricsService studentBiometricsService;
    private final HttpHeaders httpHeaders;
    private final RestTemplate restTemplate;

    private static final String FACIAL_RECOGNITION_SERVICE_API_BASE_URL = "http://127.0.0.1:8001/v1";
    private static final String FACIAL_RECOGNITION_SERVICE_API_VALIDATE_FACIAL_ENCODING_ENDPOINT = "/validate-facial-encoding";

    public StudentBiometricsController(StudentBiometricsService studentBiometricsService, HttpHeaders httpHeaders, RestTemplate restTemplate) {
        this.studentBiometricsService = studentBiometricsService;
        this.httpHeaders = httpHeaders;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/register-face/{studentNumber}")
    public ResponseEntity<String> registerFacialData(@PathVariable String studentNumber, @Valid @RequestBody FacialRegistrationRequest request) {
        if (request.getFacialEncoding() == null || request.getFacialEncoding().isEmpty()) {
            return ResponseEntity.badRequest().body("Face encoding cannot be null or empty");
        }

        log.info("Received face encoding for student {}: {} elements", studentNumber, request.getFacialEncoding().size());

        List<Double> doubleEncodings;
        try {
            doubleEncodings = request.getFacialEncoding().stream()
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Face encoding contains invalid numeric values");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("facialEncoding", doubleEncodings);

        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);

        log.debug("Sending request to facial recognition service API with {} face encoding elements", doubleEncodings.size());

        FacialEncodingResponse responseBody;
        try {
            ResponseEntity<FacialEncodingResponse> response = restTemplate.postForEntity(
                    FACIAL_RECOGNITION_SERVICE_API_BASE_URL + FACIAL_RECOGNITION_SERVICE_API_VALIDATE_FACIAL_ENCODING_ENDPOINT,
                    requestEntity,
                    FacialEncodingResponse.class
            );
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
            String errorMsg = responseBody.getMessage() != null ? responseBody.getMessage()
                    : (responseBody.getError() != null ? responseBody.getError() : "Unknown error from facial recognition service API");
            log.error("Facial recognition service API returned error: {}", errorMsg);
            return ResponseEntity.badRequest().body("Face processing failed: " + errorMsg);
        }

        List<Double> faceEncodingDoubles = responseBody.getFacialEncoding();
        if (faceEncodingDoubles == null || faceEncodingDoubles.isEmpty()) {
            log.error("Facial recognition service API returned null or empty face encoding data");
            return ResponseEntity.badRequest().body("Face processing failed: No face encoding data received");
        }

        log.info("Successfully received {} face encoding elements from facial recognition service API", faceEncodingDoubles.size());

        List<String> faceEncodingList = faceEncodingDoubles.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        try {
            Students student = studentBiometricsService.getStudentByStudentNumber(studentNumber);
            String result = studentBiometricsService.registerNewFacialBiometrics(student, faceEncodingList);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            log.warn("Facial registration conflict for student {}: {}", studentNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid facial registration request for student {}: {}", studentNumber, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error registering facial data for student {}: {}", studentNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error occurred during facial registration");
        }
    }

    @GetMapping("/status/{studentNumber}")
    public ResponseEntity<String> getFacialStatus(@PathVariable String studentNumber) {
        Optional<BiometricData> biometricData = studentBiometricsService.getFacialStatus(studentNumber);
        return biometricData.map(data -> ResponseEntity.ok("Biometric status: " + data.getBiometricsStatus()))
                .orElseGet(() -> ResponseEntity.ok("No biometric data found for student " + studentNumber));
    }

    @DeleteMapping("/recalibrate/{studentNumber}")
    public ResponseEntity<String> recalibrateFacialData(@PathVariable String studentNumber) {
        studentBiometricsService.recalibrateFacialBiometrics(studentNumber);
        return ResponseEntity.ok("Facial data recalibrated successfully");
    }
}
