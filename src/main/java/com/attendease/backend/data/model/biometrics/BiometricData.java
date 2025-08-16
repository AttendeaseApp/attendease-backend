package com.attendease.backend.data.model.biometrics;

import com.attendease.backend.data.model.enums.BiometricStatus;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

@Data
public class BiometricData {

    @DocumentId
    private String facialId;
    private String facialEncoding;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date lastUpdated;
    private BiometricStatus biometricsStatus;
}
