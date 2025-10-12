package com.attendease.backend.studentModule.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    public String studentNumber;
    public String password;
}
