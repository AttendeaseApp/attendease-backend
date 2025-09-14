package com.attendease.backend.authentication.student.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    public String studentNumber;
    public String password;
}
