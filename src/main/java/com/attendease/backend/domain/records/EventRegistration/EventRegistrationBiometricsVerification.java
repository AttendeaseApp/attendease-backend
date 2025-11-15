package com.attendease.backend.domain.records.EventRegistration;

import java.util.List;
import lombok.Data;

@Data
public class EventRegistrationBiometricsVerification {

    private Boolean success;
    private List<Float> facialEncoding;
}
