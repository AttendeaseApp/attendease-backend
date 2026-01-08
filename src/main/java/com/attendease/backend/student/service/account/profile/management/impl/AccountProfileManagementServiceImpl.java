package com.attendease.backend.student.service.account.profile.management.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.section.SectionRepository;
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
    private final SectionRepository sectionRepository;
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

            Section fullSection = null;
            if (student.getCurrentSectionId() != null) {
                fullSection = sectionRepository.findById(student.getCurrentSectionId()).orElse(null);
            } else if (student.getSection() != null) {
                fullSection = student.getSection();
            } else if (student.getSectionName() != null) {
                fullSection = sectionRepository.findBySectionName(student.getSectionName()).orElse(null);
            }

            if (fullSection != null) {
                builder.sectionId(fullSection.getId())
                        .section(fullSection.getSectionName());
                if (fullSection.getCourse() != null) {
                    builder.courseId(fullSection.getCourse().getId())
                            .course(fullSection.getCourse().getCourseName());
                    if (fullSection.getCourse().getCluster() != null) {
                        builder.clusterId(fullSection.getCourse().getCluster().getClusterId())
                                .cluster(fullSection.getCourse().getCluster().getClusterName());
                    }
                }
            } else {
                builder.section(student.getSectionName())
                        .sectionId(student.getCurrentSectionId())
                        .course(student.getCourseName())
                        .cluster(student.getClusterName());
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