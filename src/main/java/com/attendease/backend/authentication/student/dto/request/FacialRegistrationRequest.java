package com.attendease.backend.authentication.student.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacialRegistrationRequest {
    private String studentNumber;
    private List<String> facialEncoding;
}
