package com.attendease.backend.services.authentication.student;

import com.attendease.backend.data.model.students.Students;

import java.util.concurrent.ExecutionException;

public interface StudentAuthenticationInterface {
    String registerNewStudentAccount(Students student) throws Exception;
    String loginStudent(Students loginRequest) throws ExecutionException, InterruptedException;
}
