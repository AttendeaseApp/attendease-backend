package com.attendease.backend.osa.service.profile;

import com.attendease.backend.domain.users.Profiles.Profile;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OsaProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     *  Used to retrieve relevant data for OSA profile.
     *  Builds profile dto from User domain
     *
     * @return Profile object
     * */
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


    /**
     * Updates OSA password
     *
     * @param userId The student number
     * @param oldPassword   Current password for verification
     * @param newPassword   New password to set
     * @return Success message
     */
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

        validatePassword(newPassword);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password updated successfully.";
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password cannot exceed 128 characters");
        }
        if (!Pattern.compile("[A-Za-z]").matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }
    }

}
