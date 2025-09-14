package com.attendease.backend.repository.biometrics;

import com.attendease.backend.model.biometrics.BiometricData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BiometricsRepository extends MongoRepository<BiometricData, String> {
    Optional<BiometricData> findByFacialId(String facialId);

    void deleteByFacialId(String facialId);

    Optional<BiometricData> findByStudentNumber(String studentNumber);

    void deleteByStudentNumber(String studentNumber);
}
