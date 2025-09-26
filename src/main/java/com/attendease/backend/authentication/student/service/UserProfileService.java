package com.attendease.backend.authentication.student.service;

import com.attendease.backend.authentication.student.repository.AuthenticationRepository;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public Optional<Students> getStudentProfileByUserId(String userId) {
        Optional<Users> userOptional = userRepository.findById(userId);
        return userOptional.flatMap(studentRepository::findByUser);
    }

    public Optional<Users> getUserProfileByUserId(String userId) {
        return userRepository.findById(userId);
    }
}
