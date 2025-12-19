package com.attendease.backend.student.service.authentication.login;

import com.attendease.backend.domain.student.login.LoginResponse;

public interface AuthenticationLoginService {
    LoginResponse loginStudent(String studentNumber, String password);
}
