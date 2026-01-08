package com.attendease.backend.student.service.account.biometrics.management.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.biometrics.status.BiometricStatusResponse;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.student.service.account.biometrics.management.AccountBiometricsManagementService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountBiometricsManagementServiceImpl implements AccountBiometricsManagementService {

    private final BiometricsRepository biometricsRepository;
    private final StudentRepository studentRepository;

    @Override
    public Optional<BiometricStatusResponse> getFacialStatus(String authenticatedUserId) {
        Students student = studentRepository.findByUserId(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        String studentNumber = student.getStudentNumber();

        Optional<BiometricData> biometricDataOpt = biometricsRepository.findByStudentNumber(studentNumber);
        if (biometricDataOpt.isEmpty()) {
            return Optional.empty();
        }

        BiometricData biometricData = biometricDataOpt.get();
        BiometricStatusResponse response = new BiometricStatusResponse();
        response.setStatus(String.valueOf(biometricData.getBiometricsStatus()));
        response.setMessage("Facial biometrics registered");
        log.info("Facial status retrieved for student: {} (encoding NOT exposed)", studentNumber);
        return Optional.of(response);
    }

    @Override
    public void deleteFacialData(String authenticatedUserId) {
        Students student = studentRepository.findByUserId(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        String studentNumber = student.getStudentNumber();
        if (!biometricsRepository.existsByStudentNumber(studentNumber)) {
            throw new IllegalArgumentException("There is no facial biometric data in your account. It might have already been deleted, or you haven't registered it yet.");
        }
        biometricsRepository.deleteByStudentNumber(studentNumber);
        log.info("Facial biometric data deleted for student: {}", studentNumber);
    }
}
