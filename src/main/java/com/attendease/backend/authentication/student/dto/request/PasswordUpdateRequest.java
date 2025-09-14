package com.attendease.backend.authentication.student.dto.request;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String studentNumber;
    private String oldPassword;
    private String newPassword;
}
