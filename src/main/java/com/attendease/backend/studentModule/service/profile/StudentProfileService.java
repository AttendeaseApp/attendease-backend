package com.attendease.backend.studentModule.service.profile;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

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
