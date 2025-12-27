package com.attendease.backend.student.service.authentication.biometrics.registration.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.biometrics.Registration.Response.BiometricsRegistrationResponse;
import com.attendease.backend.domain.enums.BiometricStatus;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.student.service.authentication.biometrics.registration.BiometricsRegistrationService;
import com.attendease.backend.student.service.utils.BiometricImageRequestValidator;
import com.attendease.backend.student.service.utils.BiometricsRegistrationClient;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class BiometricsRegistrationServiceImpl implements BiometricsRegistrationService {

    private final StudentRepository studentRepository;
    private final BiometricsRepository biometricsRepository;
    private final BiometricImageRequestValidator imageValidator;
    private final BiometricsRegistrationClient facialRecognitionClient;

    @Override
    public ResponseEntity<String> registerFacialBiometrics(String authenticatedUserId, List<MultipartFile> images) {
        ResponseEntity<String> validationResponse = imageValidator.validateImages(images);
        if (validationResponse != null) {
            return validationResponse;
        }

        Students student = studentRepository.findByUserId(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        String studentNumber = student.getStudentNumber();

        if (biometricsRepository.existsByStudentNumber(studentNumber)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Your facial biometrics is already registered");
        }

        BiometricsRegistrationResponse biometricsRegistrationResponse;

        try {
            biometricsRegistrationResponse = facialRecognitionClient.extractFacialEncodings(images);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to process image files");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }

        if (biometricsRegistrationResponse == null || !biometricsRegistrationResponse.isSuccess()) {
            String errorMsg = (biometricsRegistrationResponse != null && biometricsRegistrationResponse.getMessage() != null)
                    ? biometricsRegistrationResponse.getMessage()
                    : "Invalid response from facial recognition service";
            return ResponseEntity.badRequest().body("Face processing failed: " + StringEscapeUtils.escapeHtml4(errorMsg));
        }

        if (biometricsRegistrationResponse.getFacialEncoding() == null || biometricsRegistrationResponse.getFacialEncoding().isEmpty()) {
            return ResponseEntity.badRequest().body("Face processing failed: No encoding data received");
        }

        try {
            List<Float> encodings = biometricsRegistrationResponse.getFacialEncoding();
            String result = saveBiometricsDataToDatabase(student, encodings);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid facial registration request: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during biometric registration for student {}: {}", studentNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    private String saveBiometricsDataToDatabase(Students student, List<Float> facialEncoding) {
        String studentNumber = student.getStudentNumber();

        if (facialEncoding == null || facialEncoding.size() != 128) {
            throw new IllegalArgumentException("Facial encoding must contain exactly 128 elements");
        }

        BiometricData biometricData = BiometricData.builder()
                .facialId(String.valueOf(UUID.randomUUID()))
                .studentNumber(studentNumber)
                .facialEncoding(facialEncoding)
                .biometricsStatus(BiometricStatus.ACTIVE)
                .build();

        biometricsRepository.save(biometricData);
        log.info("Facial biometric data saved successfully for student {}", studentNumber);
        return "Your facial biometrics registered successfully";
    }
}
