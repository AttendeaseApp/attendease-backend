package com.attendease.backend.student.service.account.profile.management.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
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
    private final BiometricsRepository biometricsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Override
    public UserStudentResponse getUserStudentProfile(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        Optional<Students> studentOpt = studentRepository.findByUser(user);

        UserStudentResponse.UserStudentResponseBuilder builder = UserStudentResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .accountStatus(user.getAccountStatus())
                .userType(user.getUserType())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (studentOpt.isPresent()) {
            Students student = studentOpt.get();
            builder.studentId(student.getId())
                    .studentNumber(student.getStudentNumber());

            if (student.getSection() != null) {
                builder.sectionId(student.getSection().getId())
                        .section(student.getSection().getSectionName());

                if (student.getSection().getCourse() != null) {
                    builder.courseId(student.getSection().getCourse().getId())
                            .course(student.getSection().getCourse().getCourseName());

                    if (student.getSection().getCourse().getCluster() != null) {
                        builder.clusterId(student.getSection().getCourse().getCluster().getClusterId())
                                .cluster(student.getSection().getCourse().getCluster().getClusterName());
                    }
                }
            }

            /*if only available, this data will be added*/
            Optional<BiometricData> biometricOpt = biometricsRepository.findByStudentNumber(student.getStudentNumber());
            if (biometricOpt.isPresent()) { BiometricData biometric = biometricOpt.get();
                builder
                        .biometricId(biometric.getFacialId())
                        .biometricStatus(biometric.getBiometricsStatus())
                        .biometricCreatedAt(biometric.getCreatedAt())
                        .biometricLastUpdated(biometric.getLastUpdated())
                        .hasBiometricData(true);
            } else {
                builder.hasBiometricData(false);
            }
        }
        return builder.build();
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
