package com.attendease.backend.repository.biometrics;

import com.attendease.backend.domain.biometrics.BiometricData;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on {@link BiometricData} documents in MongoDB.
 * <p>
 * This interface extends {@link MongoRepository} to leverage Spring Data MongoDB functionality,
 * providing standard operations like {@code save}, {@code findAll}, {@code findById}, {@code delete},
 * as well as custom query methods specific to student facial biometrics.
 * </p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Repository
public interface BiometricsRepository extends MongoRepository<BiometricData, String> {
    /**
     * Deletes a biometric record by its unique facial ID.
     *
     * @param facialId the unique identifier of the facial biometric data to delete
     */
    void deleteByFacialId(String facialId);

    /**
     * Retrieves a biometric record using a student's unique student number.
     *
     * @param studentNumber the unique identifier of the student
     * @return an {@link Optional} containing the {@link BiometricData} if found, or empty if not
     */
    Optional<BiometricData> findByStudentNumber(String studentNumber);

    /**
     * Deletes a biometric record by the student's unique student number.
     *
     * @param studentNumber the unique identifier of the student whose biometric data should be deleted
     */
    void deleteByStudentNumber(String studentNumber);
}
