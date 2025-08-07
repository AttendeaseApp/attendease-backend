package com.attendease.attendease_backend.repository.authentication.student;

import com.attendease.attendease_backend.data.student.Student;
import com.attendease.attendease_backend.data.user.User;

import java.util.Optional;

public interface StudentAuthenticationRepository {
    Student findByCredentials(String studentNumber, String password);
    void saveWithTransaction(Student student, User user);
    Optional<Student> findByStudentNumber(String studentNumber);
    boolean existsByEmail(String email);
    boolean existsByStudentNumber(String studentNumber);
}
