package com.attendease.backend.student.service.authentication.login;


import com.attendease.backend.student.service.authentication.login.impl.AuthenticationLoginServiceImpl;

public interface AuthenticationLoginService {

    AuthenticationLoginServiceImpl.LoginResult loginStudent(String studentNumber, String password);
}
