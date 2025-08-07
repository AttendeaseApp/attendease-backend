package com.attendease.attendease_backend.services.authentication.student;

import com.attendease.attendease_backend.data.student.Student;

import java.util.concurrent.ExecutionException;

public interface StudentAuthenticationInterface {
    String registerNewStudentAccount(Student student) throws Exception;
    String loginStudent(Student loginRequest) throws ExecutionException, InterruptedException;
}
