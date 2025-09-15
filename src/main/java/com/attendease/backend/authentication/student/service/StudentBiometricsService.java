package com.attendease.backend.authentication.student.service;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.enums.BiometricStatus;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentBiometricsService {

    private final StudentRepository studentsRepository;
    private final BiometricsRepository biometricsRepository;


    public Students getStudentByStudentNumber(String studentNumber) {
        return studentsRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with student number: " + studentNumber));
    }

    public String registerNewFacialBiometrics(Students student, List<String> facialEncoding) {
        String studentNumber = student.getStudentNumber();

        Optional<BiometricData> existing = biometricsRepository.findByStudentNumber(studentNumber);
        if (existing.isPresent()) {
            throw new IllegalStateException("Facial biometrics already registered for this student.");
        }

        BiometricData biometricData = new BiometricData();
        biometricData.setFacialId(UUID.randomUUID().toString());
        biometricData.setStudentNumber(studentNumber);
        biometricData.setFacialId("face_" + studentNumber);
        biometricData.setFacialEncoding(facialEncoding);
        biometricData.setBiometricsStatus(BiometricStatus.ACTIVE);

        biometricsRepository.save(biometricData);

        log.info("Facial biometrics registered for student: {}", studentNumber);

        return "Facial biometrics registered successfully";
    }

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
