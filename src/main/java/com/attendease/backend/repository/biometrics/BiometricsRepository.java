package com.attendease.backend.repository.biometrics;

import com.attendease.backend.domain.biometrics.BiometricData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BiometricsRepository extends MongoRepository<BiometricData, String> {

    void deleteByFacialId(String facialId);

    Optional<BiometricData> findByStudentNumber(String studentNumber);

    void deleteByStudentNumber(String studentNumber);
}
