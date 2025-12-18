package com.attendease.backend.domain.biometrics.Registration.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * Represents the response from the facial biometrics registration service.
 * <p>
 * This class encapsulates the result of registering a student's facial biometrics,
 * including the status, messages, and extracted facial encoding data.
 * </p>
 * <p>
 * Typical usage occurs in {@link com.attendease.backend.student.service.authentication.biometrics.BiometricsRegistrationService#registerFacialBiometrics(String, List)}
 * where this response is returned by the {@link com.attendease.backend.student.service.utils.BiometricsRegistrationClient#extractFacialEncodings(List)} method.
 * </p>
 */
@Data
public class BiometricsRegistrationResponse {

    private boolean success;
    private String message;

    @JsonProperty("facialEncoding")
    private List<Float> facialEncoding;

    private String error;
}
