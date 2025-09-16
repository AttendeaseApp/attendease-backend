package com.attendease.backend.domain.biometrics;

import com.attendease.backend.domain.enums.BiometricStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Class representing biometric data for a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "biometric_data")
public class BiometricData {

    @Id
    private String facialId;

    @NotNull(message = "Facial encoding is required")
    @Size(min = 128, max = 128, message = "Facial encoding must have exactly 128 elements")
    private List<String> facialEncoding;

    private String studentNumber;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastUpdated;

    @Builder.Default
    private BiometricStatus biometricsStatus = BiometricStatus.PENDING;
}
