package com.attendease.backend.osa.service.profile.impl;

import com.attendease.backend.domain.users.Profiles.Profile;
import com.attendease.backend.domain.users.Users;
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
    public Optional<Profile> getOsaProfileByUserId(String userId) {
        return userRepository.findById(userId)
                .map(user -> Profile.builder()
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

        Users user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        userValidator.validatePassword(newPassword);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password updated successfully.";
    }
}
