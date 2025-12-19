package com.attendease.backend.student.service.account.profile.management;

import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;

import java.util.Optional;

/**
 * Service class responsible for managing student personal profile operations,
 * including retrieving profile information and updating passwords.
 *
 * <p>This service interacts with both {@link UserRepository} and
 * {@link StudentRepository} to fetch and update data related to student
 * and their associated user account.</p>
 */
public interface AccountProfileManagementService {

    /**
     * Retrieves a student's personal profile using the associated user's ID.
     *
     * @param userId the ID of the user whose student profile is requested
     * @return an {@link Optional} containing the student profile if found, otherwise empty
     */
    Optional<Students> getStudentProfileByUserId(String userId);

    /**
     * Retrieves a user profile by user ID.
     *
     * @param userId the ID of the user
     * @return an {@link Optional} containing the user profile if found, otherwise empty
     */
    Optional<User> getUserProfileByUserId(String userId);

    /**
     * Updates student password
     *
     * @param id The student number
     * @param oldPassword   Current password for verification
     * @param newPassword   New password to set
     * @return Success message
     */
    String updatePassword(String id, String oldPassword, String newPassword);
}
