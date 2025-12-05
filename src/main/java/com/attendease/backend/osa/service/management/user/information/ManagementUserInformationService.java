package com.attendease.backend.osa.service.management.user.information;

import com.attendease.backend.domain.users.Information.Management.Request.UpdateUserRequest;
import com.attendease.backend.domain.users.Information.Management.Response.UpdateResultResponse;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link ManagementUserInformationService} is a service responsible for managing user information updates and bulk deletions.
 *
 * <p>Provides methods for OSA (Office of Student Affairs) operations, including updating user details with audit trails
 * and deleting all students along with associated data. Ensures validations, password encoding, and propagation to linked entities
 * (e.g., Students, Sections). Transactions are used for atomicity in updates.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-26
 */
public interface ManagementUserInformationService {

    /**
     * {@code deleteAllStudentsAndAssociatedUserAndFacialData} is used to permanently delete all students along with their associated user accounts and facial biometrics data.
     *
     * <p>This method cascades deletions across collections (Students, Users, StudentBiometrics) for complete cleanup.
     * Intended for administrative purposes like database resets.</p>
     *
     * @return the count of deleted students
     */
    long deleteAllStudentsAndAssociatedUserAndFacialData();

    /**
     * {@code osaUpdateUserInfo} allows OSA to update user information (with optional student-specific fields).
     * User field updates automatically propagate to referenced students via DBRef.
     * Student-specific fields require explicit update to the Students collection.
     *
     * <p>Validates inputs (e.g., password strength, unique student number), encodes passwords, and sets audit fields.
     * For students, derives section/course/cluster details in the response. Captures the updater's ID for traceability.</p>
     *
     * @param userId the unique ID of the user to update
     * @param request the {@link UpdateUserRequest} containing optional fields to update
     * @param updatedByUserId the ID of the OSA user performing the update (for audit)
     * @return the {@link UpdateResultResponse} containing the updated user and optional student response
     * @throws ChangeSetPersister.NotFoundException if the user, student, or referenced entities (e.g., section) are not found
     * @throws IllegalArgumentException if validation fails (e.g., invalid password, duplicate student number)
     */
    @Transactional
    UpdateResultResponse osaUpdateUserInfo(String userId, UpdateUserRequest request, String updatedByUserId) throws ChangeSetPersister.NotFoundException;
}
