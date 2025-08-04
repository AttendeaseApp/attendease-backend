package com.attendease.attendease_backend.data.biometrics;

import com.attendease.attendease_backend.enums.BiometricStatus;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

@Data
public class Biometrics {

    @DocumentId
    private String facialId;
    private String facialEncoding;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date lastUpdated;
    private BiometricStatus biometricsStatus;
}
