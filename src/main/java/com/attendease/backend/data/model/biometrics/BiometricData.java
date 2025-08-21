package com.attendease.backend.data.model.biometrics;

import com.attendease.backend.data.model.enums.BiometricStatus;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BiometricData {

    @DocumentId
    private String facialId;
    private List<String> facialEncoding;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date lastUpdated;
    private BiometricStatus biometricsStatus;

    public void setFacialEncoding(List<String> facialEncoding) {
        if (facialEncoding == null || facialEncoding.isEmpty()) {
            throw new IllegalArgumentException("Facial encoding cannot be null or empty");
        }
        if (facialEncoding.size() != 128) {
            throw new IllegalArgumentException("Facial encoding must have exactly 128 elements");
        }
        this.facialEncoding = facialEncoding;
    }

    public BiometricStatus getBiometricsStatus() {
        return biometricsStatus != null ? biometricsStatus : BiometricStatus.PENDING;
    }
}
