package com.attendease.backend.repository.students;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends MongoRepository<Students, String> {
    Optional<Students> findByStudentNumber(String studentNumber);
    boolean existsByStudentNumber(String studentNumber);
    List<Students> findByUserIn(List<Users> users);
    List<Students> findByIdIn(List<String> studentIds);

}
