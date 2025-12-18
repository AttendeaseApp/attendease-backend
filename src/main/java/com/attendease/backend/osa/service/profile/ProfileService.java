package com.attendease.backend.osa.service.profile;

import com.attendease.backend.domain.user.account.profile.UserAccountProfile;

import java.util.Optional;

public interface ProfileService {

    /**
     * {@code getOsaProfileByUserId} is used to retrieve relevant data for osa profile.
     * Builds profile dto from User domain
     *
     * @return UserAccountProfile object
     */
    Optional<UserAccountProfile> getOsaProfileByUserId(String userId);

    /**
     * {@code updatePassword} is used to update osa own password
     *
     * @param userId The student number
     * @param oldPassword   Current password for verification
     * @param newPassword   New password to set
     * @return Success message
     */
    String updatePassword(String userId, String oldPassword, String newPassword);
}
