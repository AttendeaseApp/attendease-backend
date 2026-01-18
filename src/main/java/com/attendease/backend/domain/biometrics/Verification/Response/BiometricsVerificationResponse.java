package com.attendease.backend.domain.biometrics.Verification.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the result of comparing two facial encodings using the biometrics verification service.
 *
 * <p>This DTO contains the outcome of the verification process, including:</p>
 * <ul>
 *     <li>{@code is_face_matched} — whether the uploaded encoding matches the stored reference encoding</li>
 *     <li>{@code face_distance} — Euclidean distance between encodings (lower = more similar)</li>
 *     <li>{@code confidence} — confidence score (1.0 - distance), higher = more confident match</li>
 * </ul>
 */
@Data
public class BiometricsVerificationResponse {

    @JsonProperty("is_face_matched")
    private Boolean is_face_matched;

    @JsonProperty("face_distance")
    private Double face_distance;

    private Double confidence;

    private Boolean success;

    private Boolean verified;
}