package com.attendease.backend.authentication.student.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class FacialVerificationRequest {
    private List<String> facialEncoding;
}
