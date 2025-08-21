package com.attendease.backend.services.authentication.biometrics;

import com.attendease.backend.data.model.biometrics.BiometricData;
import com.attendease.backend.data.model.enums.BiometricStatus;
import com.attendease.backend.data.model.students.Students;
import com.attendease.backend.repository.biometrics.FacialAuthenticationRepository;
import com.attendease.backend.repository.student.StudentRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FacialAuthenticationService {

    private final Firestore firestore;
    private final StudentRepository studentRepository;
    private final FacialAuthenticationRepository facialAuthenticationRepository;

    public FacialAuthenticationService(Firestore firestore,StudentRepository studentRepository, FacialAuthenticationRepository facialAuthenticationRepository) {
        this.firestore = firestore;
        this.studentRepository = studentRepository;
        this.facialAuthenticationRepository = facialAuthenticationRepository;
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

            facialAuthenticationRepository.saveBiometricData(biometricData);

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
            return facialAuthenticationRepository.findBiometricDataByFacialId(student.getStudentNumber());
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

            facialAuthenticationRepository.deleteBiometricData(student.getStudentNumber() + "_facial");
            student.setFacialRefID(null);
            studentRepository.saveStudent(student);
        } catch (Exception e) {
            throw new Exception("Facial recalibration failed: " + e.getMessage(), e);
        }
    }

}
