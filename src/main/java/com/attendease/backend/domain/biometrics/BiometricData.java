package com.attendease.backend.domain.biometrics;

import com.attendease.backend.domain.enums.BiometricStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain entity representing biometric data for users (primarily students).
 * <p>
 * This entity stores facial recognition encodings (as a fixed-size vector of 128 floats) for secure authentication.
 * It supports facial login during event registration and attendance tracking. Status tracks enrollment/validation.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
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
    private List<Float> facialEncoding;

    private String studentNumber;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;

    @Builder.Default
    private BiometricStatus biometricsStatus = BiometricStatus.PENDING;
}
