package com.attendease.backend.domain.biometrics.Verification.Response;

import java.util.Map;

import com.attendease.backend.client.biometrics.verification.BiometricsVerificationClient;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("facialEncoding")
    private java.util.List<Float> facialEncoding;

    private String message;

    private Map<String, Object> metadata;

    /**
     * Helper method to get the quality score from metadata
     */
    public Double getQuality() {
        if (metadata != null && metadata.containsKey("average_quality")) {
            Object quality = metadata.get("average_quality");
            if (quality instanceof Number) {
                return ((Number) quality).doubleValue();
            }
        }
        return null;
    }
}
