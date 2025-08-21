package com.attendease.backend.repository.biometrics;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;
import org.springframework.stereotype.Repository;

import com.attendease.backend.data.model.biometrics.BiometricData;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class FacialAuthenticationRepository {

    private final Firestore firestore;

    public FacialAuthenticationRepository(Firestore firestore){
        this.firestore = firestore;
    }

    public void saveBiometricData(BiometricData biometricData) throws ExecutionException, InterruptedException {
        try {
            ApiFuture<WriteResult> future = firestore.collection("biometricsData").document(biometricData.getFacialId()).set(biometricData);

            WriteResult result = future.get(10, TimeUnit.SECONDS);
            log.debug("Biometric data saved successfully at: {}", result.getUpdateTime());

        } catch (TimeoutException e) {
            log.error("Timeout while saving biometric data for ID: {}", biometricData.getFacialId());
            throw new RuntimeException("Database operation timed out", e);
        } catch (Exception e) {
            log.error("Error saving biometric data for ID: {}", biometricData.getFacialId(), e);
            throw e;
        }
    }



    public Optional<BiometricData> findBiometricDataByFacialId(String facialId) {
        try {
            DocumentSnapshot document = firestore.collection("biometricsData").document(facialId).get().get();
            if (document.exists()) {
                BiometricData biometricData = document.toObject(BiometricData.class);
                assert biometricData != null;
                return Optional.of(biometricData);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to find biometric data by facialId {}: {}", facialId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve biometric data", e);
        }
    }

    public void deleteBiometricData(String facialId) throws ExecutionException, InterruptedException {
        firestore.collection("biometricsData").document(facialId).delete().get();
    }
}
