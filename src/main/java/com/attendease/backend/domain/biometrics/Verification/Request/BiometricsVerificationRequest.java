package com.attendease.backend.domain.biometrics.Verification.Request;

import java.util.List;
import lombok.Data;

/**
 * Represents a request to verify if two facial encodings match, typically used for facial recognition authentication.
 *
 * <p>This DTO is sent to the biometrics verification service to compare:</p>
 * <ul>
 *     <li>{@code uploaded_encoding} — the facial encoding derived from an uploaded image</li>
 *     <li>{@code reference_encoding} — the stored facial encoding that is pre-registered for the student</li>
 * </ul>
 *
 * <p>Used by the {@link com.attendease.backend.studentModule.service.utils.BiometricsVerificationClient}
 * to confirm if the uploaded face matches the pre-registered face.</p>
 */
@Data
public class BiometricsVerificationRequest {

    private List<Float> uploaded_encoding;
    private List<Float> reference_encoding;
}
