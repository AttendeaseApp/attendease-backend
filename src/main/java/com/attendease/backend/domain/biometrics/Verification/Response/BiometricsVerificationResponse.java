package com.attendease.backend.domain.biometrics.Verification.Response;

import com.attendease.backend.client.biometrics.verification.BiometricsVerificationClient;
import lombok.Data;

/**
 * Represents the result of comparing two facial encodings using the biometrics verification service.
 *
 * <p>This DTO contains the outcome of the verification process, including:</p>
 * <ul>
 *     <li>{@code success} — whether the verification request was processed successfully</li>
 *     <li>{@code verified} — general verification flag (may be used for extended workflows)</li>
 *     <li>{@code is_face_matched} — indicates whether the uploaded encoding matches the stored reference encoding</li>
 * </ul>
 *
 * <p>Used primarily by {@link BiometricsVerificationClient}.</p>
 */
@Data
public class BiometricsVerificationResponse {

    private Boolean success;
    private Boolean verified;
    private Boolean is_face_matched;
}
