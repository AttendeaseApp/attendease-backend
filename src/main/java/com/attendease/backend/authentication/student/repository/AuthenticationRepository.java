package com.attendease.backend.authentication.student.repository;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Getter
@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthenticationRepository {

    private final StudentRepository studentsRepository;
    private final UserRepository usersRepository;

    public boolean existsByStudentNumber(String studentNumber) {
        return studentsRepository.existsByStudentNumber(studentNumber);
    }

    public void saveStudent(Students student) {
        if (student.getStudentNumber() == null || student.getStudentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required");
        }
        studentsRepository.save(student);
        log.info("Student saved successfully: {}", student.getStudentNumber());
    }

    public void saveUser(Users user) {
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        usersRepository.save(user);
        log.info("User saved successfully: {}", user.getUserId());
    }

    public void updateUser(Users user) {
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required for update");
        }
        usersRepository.save(user);
        log.info("User updated successfully: {}", user.getUserId());
    }

    public Optional<Students> findByStudentNumber(String studentNumber) {
        return studentsRepository.findByStudentNumber(studentNumber);
    }
}
