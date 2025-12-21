package com.attendease.backend.student.service.account.biometrics.management.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.student.service.account.biometrics.management.AccountBiometricsManagementService;
import com.attendease.backend.student.service.authentication.biometrics.registration.impl.BiometricsRegistrationServiceImpl;
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
    public Optional<BiometricData> getFacialStatus(String authenticatedUserId) {
        Students student = studentRepository.findByUserId(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        String studentNumber = student.getStudentNumber();
        if (!biometricsRepository.existsByStudentNumber(studentNumber)) {
            throw new IllegalArgumentException("No facial biometric data found for this student " + studentNumber);
        }
        return biometricsRepository.findByStudentNumber(studentNumber);
    }

    @Override
    public void deleteFacialData(String authenticatedUserId) {
        Students student = studentRepository.findByUserId(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        String studentNumber = student.getStudentNumber();
        if (!biometricsRepository.existsByStudentNumber(studentNumber)) {
            throw new IllegalArgumentException("No facial biometric data found for this student");
        }
        biometricsRepository.deleteByStudentNumber(studentNumber);
        log.info("Facial biometric data deleted for student: {}", studentNumber);
    }
}
