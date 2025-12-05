package com.attendease.backend.osa.service.profile;

import com.attendease.backend.domain.users.Profiles.Profile;

import java.util.Optional;

public interface ProfileService {

    /**
     * {@code getOsaProfileByUserId} is used to retrieve relevant data for OSA profile.
     * Builds profile dto from User domain
     *
     * @return Profile object
     */
    Optional<Profile> getOsaProfileByUserId(String userId);

    /**
     * {@code updatePassword} is used to update OSA own password
     *
     * @param userId The student number
     * @param oldPassword   Current password for verification
     * @param newPassword   New password to set
     * @return Success message
     */
    String updatePassword(String userId, String oldPassword, String newPassword);
}
