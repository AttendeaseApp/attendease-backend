package com.attendease.backend.student.service.account.profile.management;

import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;


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
     * Retrieves a complete user-student profile by user ID
     * @param userId the authenticated user's ID
     * @return UserStudentResponse with all profile data, or null if not found
     */
    UserStudentResponse getUserStudentProfile(String userId);

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
