package com.attendease.backend.domain.students.Login.Request;

import lombok.Data;

@Data
public class LoginRequest {
    public String studentNumber;
    public String password;
}
