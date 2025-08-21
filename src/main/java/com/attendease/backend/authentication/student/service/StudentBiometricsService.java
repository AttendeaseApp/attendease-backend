package com.attendease.backend.authentication.student.service;

import com.attendease.backend.model.biometrics.BiometricData;
import com.attendease.backend.model.enums.BiometricStatus;
import com.attendease.backend.model.students.Students;
import com.attendease.backend.authentication.student.repository.StudentBiometricsRepository;
import com.attendease.backend.authentication.student.repository.StudentAuthenticationRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StudentBiometricsService {

    private final Firestore firestore;
    private final StudentAuthenticationRepository studentRepository;
    private final StudentBiometricsRepository studentBiometricsRepository;

    public StudentBiometricsService(Firestore firestore, StudentAuthenticationRepository studentRepository, StudentBiometricsRepository studentBiometricsRepository) {
        this.firestore = firestore;
        this.studentRepository = studentRepository;
        this.studentBiometricsRepository = studentBiometricsRepository;
    }

    public Students getStudentByStudentNumber(String studentNumber) throws Exception {
        return studentRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new Exception("Student with number " + studentNumber + " not found"));
    }

    public String registerNewFacialBiometrics(Students student, List<String> facialEncoding) throws Exception {
        try {
            if (student.getFacialRefID() != null) {
                throw new IllegalStateException("Facial biometric data already exists for student " + student.getStudentNumber());
            }

            if (facialEncoding == null || facialEncoding.isEmpty()) {
                throw new IllegalArgumentException("Facial encoding cannot be null or empty");
            }

            if (facialEncoding.size() != 128) {
                throw new IllegalArgumentException("Facial encoding must have exactly 128 elements, got: " + facialEncoding.size());
            }

            BiometricData biometricData = new BiometricData();
            String facialId = student.getStudentNumber() + "_facial_" + System.currentTimeMillis();
            biometricData.setFacialId(facialId);
            biometricData.setFacialEncoding(facialEncoding);
            biometricData.setBiometricsStatus(BiometricStatus.ACTIVE);

            studentBiometricsRepository.saveBiometricData(biometricData);

            DocumentReference biometricRef = firestore.collection("biometricsData").document(facialId);
            student.setFacialRefID(biometricRef);

            studentRepository.saveStudent(student);

            log.info("Successfully registered facial biometrics for student: {}", student.getStudentNumber());
            return "Student facial biometrics registered successfully";

        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to register facial biometrics for student {}: {}", student.getStudentNumber(), e.getMessage(), e);
            throw new Exception("Failed to register student biometrics: " + e.getMessage(), e);
        }
    }

    public Optional<BiometricData> getFacialStatus(String studentNumber) throws Exception {
        try {
            Students student = getStudentByStudentNumber(studentNumber);
            if (student.getFacialRefID() == null) {
                return Optional.empty();
            }
            return studentBiometricsRepository.findBiometricDataByFacialId(student.getStudentNumber());
        } catch (Exception e) {
            throw new Exception("Failed to retrieve facial status: " + e.getMessage(), e);
        }
    }

    public void recalibrateFacialBiometrics(String studentNumber) throws Exception {
        try {
            Students student = getStudentByStudentNumber(studentNumber);
            if (student.getFacialRefID() == null) {
                throw new IllegalStateException("No biometric data found for student " + studentNumber);
            }

            studentBiometricsRepository.deleteBiometricData(student.getStudentNumber() + "_facial");
            student.setFacialRefID(null);
            studentRepository.saveStudent(student);
        } catch (Exception e) {
            throw new Exception("Facial recalibration failed: " + e.getMessage(), e);
        }
    }

}
