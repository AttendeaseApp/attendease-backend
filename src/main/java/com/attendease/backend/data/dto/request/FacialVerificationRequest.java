package com.attendease.backend.data.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class FacialVerificationRequest {
    private List<String> facialEncoding;
}
