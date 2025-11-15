package com.attendease.backend.domain.students.Request;

import lombok.Data;

@Data
public class LoginRequest {
    public String studentNumber;
    public String password;
}
