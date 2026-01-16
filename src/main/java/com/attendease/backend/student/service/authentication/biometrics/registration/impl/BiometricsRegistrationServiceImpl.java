package com.attendease.backend.student.service.authentication.biometrics.registration.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.biometrics.Registration.Response.BiometricsRegistrationResponse;
import com.attendease.backend.domain.enums.BiometricStatus;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.exceptions.domain.Biometrics.*;
import com.attendease.backend.exceptions.domain.Biometrics.Registration.BiometricAlreadyRegisteredException;
import com.attendease.backend.exceptions.domain.Biometrics.Registration.BiometricProcessingException;
import com.attendease.backend.exceptions.domain.Student.StudentNotFoundException;
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
    public void registerFacialBiometrics(String authenticatedUserId, List<MultipartFile> images) {
        imageValidator.validateImages(images);
        Students student = studentRepository.findByUserId(authenticatedUserId).orElseThrow(() -> new StudentNotFoundException("Student record not found for authenticated user"));

        String studentNumber = student.getStudentNumber();

        if (biometricsRepository.existsByStudentNumber(studentNumber)) {
            throw new BiometricAlreadyRegisteredException("Your facial biometrics is already registered");
        }

        BiometricsRegistrationResponse biometricsRegistrationResponse;

        try {
            biometricsRegistrationResponse = facialRecognitionClient.extractFacialEncodings(images);
        } catch (IOException e) {
            throw new BiometricProcessingException("Failed to process image files", e);
        } catch (IllegalStateException e) {
            throw new FacialRecognitionServiceException(e.getMessage(), e);
        }

        if (biometricsRegistrationResponse == null || !biometricsRegistrationResponse.isSuccess()) {
            String errorMsg = (biometricsRegistrationResponse != null && biometricsRegistrationResponse.getMessage() != null)
                    ? biometricsRegistrationResponse.getMessage()
                    : "Invalid response from facial recognition service";
            throw new BiometricProcessingException("Face processing failed: " + StringEscapeUtils.escapeHtml4(errorMsg));
        }

        if (biometricsRegistrationResponse.getFacialEncoding() == null || biometricsRegistrationResponse.getFacialEncoding().isEmpty()) {
            throw new BiometricProcessingException("Face processing failed: No encoding data received");
        }

        List<Float> encodings = biometricsRegistrationResponse.getFacialEncoding();
        saveBiometricsDataToDatabase(student, encodings);
        log.info("Facial biometric data saved successfully for student {}", studentNumber);
    }

    private void saveBiometricsDataToDatabase(Students student, List<Float> facialEncoding) {
        String studentNumber = student.getStudentNumber();

        if (facialEncoding == null || facialEncoding.size() != 128) {
            throw new InvalidFacialEncodingException("Facial encoding must contain exactly 128 elements");
        }

        BiometricData biometricData = BiometricData.builder()
                .facialId(String.valueOf(UUID.randomUUID()))
                .studentNumber(studentNumber)
                .facialEncoding(facialEncoding)
                .biometricsStatus(BiometricStatus.ACTIVE)
                .build();

        biometricsRepository.save(biometricData);
    }
}