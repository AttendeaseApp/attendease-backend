package com.attendease.backend.authentication.student.controller;

import com.attendease.backend.authentication.student.dto.request.FacialRegistrationRequest;
import com.attendease.backend.authentication.student.dto.response.FacialEncodingResponse;
import com.attendease.backend.model.biometrics.BiometricData;
import com.attendease.backend.model.students.Students;
import com.attendease.backend.authentication.student.service.StudentBiometricsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("v1/api/auth/biometrics")
@Slf4j
public class StudentBiometricsController {

    private final StudentBiometricsService studentBiometricsService;
    private final HttpHeaders httpHeaders;
    private final RestTemplate restTemplate;

    private static final String FACIAL_RECOGNITION_SERVICE_API_BASE_URL = "http://127.0.0.1:8001/v1";
    private static final String FACIAL_RECOGNITION_SERVICE_API_VALIDATE_FACIAL_ENCODING_ENDPOINT = "/validate-facial-encoding";
    private static final String FACIAL_RECOGNITION_SERVICE_API_VERIFY_FACIAL_ENCODING_ENDPOINT = "/authenticate-face";

    public StudentBiometricsController(StudentBiometricsService studentBiometricsService, HttpHeaders httpHeaders, RestTemplate restTemplate) {
        this.studentBiometricsService = studentBiometricsService;
        this.httpHeaders = httpHeaders;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/register-face/{studentNumber}")
    public ResponseEntity<String> registerFacialData(@PathVariable String studentNumber,@Valid @RequestBody FacialRegistrationRequest request) {
        try {
            if (request.getFacialEncoding() == null || request.getFacialEncoding().isEmpty()) {
                return ResponseEntity.badRequest().body("Face encoding cannot be null or empty");
            }

            log.info("Received face encoding for student {}: {} elements", studentNumber, request.getFacialEncoding().size());

            List<Double> doubleEncodings;

            try {
                doubleEncodings = request.getFacialEncoding().stream().map(Double::parseDouble).collect(Collectors.toList());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Face encoding contains invalid numeric values");
            }

            Map<String, Object> body = new HashMap<>();
            body.put("facialEncoding", doubleEncodings);

            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);

            log.debug("Sending request to FACIAL RECOGNITION SERVICE API with {} face encoding elements", doubleEncodings.size());

            ResponseEntity<FacialEncodingResponse> response;

            try {
                response = restTemplate.postForEntity(FACIAL_RECOGNITION_SERVICE_API_BASE_URL + FACIAL_RECOGNITION_SERVICE_API_VALIDATE_FACIAL_ENCODING_ENDPOINT, requestEntity, FacialEncodingResponse.class);
            } catch (Exception e) {
                log.error("Failed to communicate with FACIAL RECOGNITION SERVICE API: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Face processing service is unavailable");
            }

            if (response.getBody() == null) {
                log.error("FACIAL RECOGNITION SERVICE returned null response body");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Face processing service returned invalid response");
            }

            FacialEncodingResponse responseBody = response.getBody();

            if (!responseBody.isSuccess()) {
                String errorMsg = responseBody.getMessage() != null ? responseBody.getMessage() :
                        (responseBody.getError() != null ? responseBody.getError() : "Unknown error from FACIAL RECOGNITION SERVICE API");
                log.error("FACIAL RECOGNITION SERVICE API returned error: {}", errorMsg);
                return ResponseEntity.badRequest().body("Face processing failed: " + errorMsg);
            }

            List<Double> faceEncodingDoubles = responseBody.getFacialEncoding();
            if (faceEncodingDoubles == null || faceEncodingDoubles.isEmpty()) {
                log.error("FACIAL RECOGNITION SERVICE API returned null or empty face encoding data");
                return ResponseEntity.badRequest().body("Face processing failed: No face encoding data received");
            }

            log.info("Successfully received {} face encoding elements from FACIAL RECOGNITION SERVICE API", faceEncodingDoubles.size());

            List<String> faceEncodingList = faceEncodingDoubles.stream().map(String::valueOf).collect(Collectors.toList());

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
        try {
            Optional<BiometricData> biometricData = studentBiometricsService.getFacialStatus(studentNumber);
            return biometricData.map(data -> ResponseEntity.ok("Biometric status: " + data.getBiometricsStatus())).orElseGet(() -> ResponseEntity.ok("No biometric data found for student " + studentNumber));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/recalibrate/{studentNumber}")
    public ResponseEntity<String> recalibrateFacialData(@PathVariable String studentNumber) {
        try {
            studentBiometricsService.recalibrateFacialBiometrics(studentNumber);
            return ResponseEntity.ok("Facial data recalibrated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
