package com.attendease.backend.domain.biometrics.Verification.Response;

import java.util.List;

import com.attendease.backend.client.biometrics.verification.BiometricsVerificationClient;
import lombok.Data;

/**
 * Represents the response returned by the biometrics service when extracting
 * a facial encoding from an uploaded Base64 image during event registration.
 *
 * <p>This DTO contains:</p>
 * <ul>
 *     <li>{@code success} — whether the facial detection and encoding extraction succeeded</li>
 *     <li>{@code facialEncoding} — the extracted facial encoding represented as a list of floats</li>
 * </ul>
 *
 * <p>Used primarily by {@link BiometricsVerificationClient}.</p>
 */
@Data
public class EventRegistrationBiometricsVerificationResponse {

    private Boolean success;
    private List<Float> facialEncoding;
}
