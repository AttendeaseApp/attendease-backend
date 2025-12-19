package com.attendease.backend.osa.service.profile.impl;

import com.attendease.backend.domain.user.account.profile.UserAccountProfile;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.osa.service.profile.ProfileService;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Override
    public Optional<UserAccountProfile> getOsaProfileByUserId(String userId) {
        return userRepository.findById(userId)
                .map(user -> UserAccountProfile.builder()
                        .userId(user.getUserId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .contactNumber(user.getContactNumber())
                        .email(user.getEmail())
                        .accountStatus(user.getAccountStatus())
                        .userType(user.getUserType())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build()
                );
    }

    @Override
    public String updatePassword(String userId, String oldPassword, String newPassword) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User id is required.");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        userValidator.validatePassword(newPassword);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "password updated successfully.";
    }
}
