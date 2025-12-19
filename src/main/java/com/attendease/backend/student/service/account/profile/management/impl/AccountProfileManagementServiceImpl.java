package com.attendease.backend.student.service.account.profile.management.impl;

import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import java.util.Optional;

import com.attendease.backend.student.service.account.profile.management.AccountProfileManagementService;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountProfileManagementServiceImpl implements AccountProfileManagementService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Override
    public Optional<Students> getStudentProfileByUserId(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.flatMap(studentRepository::findByUser);
    }

    @Override
    public Optional<User> getUserProfileByUserId(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public String updatePassword(String id, String oldPassword, String newPassword) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required.");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }

        userValidator.validatePassword(newPassword);

        Students student = studentRepository.findByUserId(id).orElseThrow(() -> new IllegalArgumentException("Student not found."));
        User user = student.getUser();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "password updated successfully.";
    }
}
