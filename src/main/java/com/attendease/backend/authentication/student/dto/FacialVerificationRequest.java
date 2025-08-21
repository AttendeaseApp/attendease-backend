package com.attendease.backend.authentication.student.dto;

import lombok.Data;
import java.util.List;

@Data
public class FacialVerificationRequest {
    private List<String> facialEncoding;
}
