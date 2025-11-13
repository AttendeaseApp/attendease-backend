package com.attendease.backend.studentModule.service.profile;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BiometricsManagementService {

    private final BiometricsRepository biometricsRepository;

    public Optional<BiometricData> getFacialStatus(String studentNumber) {
        return biometricsRepository.findByStudentNumber(studentNumber);
    }

    public void recalibrateFacialBiometrics(String studentNumber) {
        Optional<BiometricData> existing = biometricsRepository.findByStudentNumber(studentNumber);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("No facial biometric data found for this student.");
        }

        biometricsRepository.deleteByStudentNumber(studentNumber);

        log.info("Facial biometric data deleted for student: {}", studentNumber);
    }
}
