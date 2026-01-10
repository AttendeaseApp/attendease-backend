package com.attendease.backend.osa.controller.management.user.information;

import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.account.management.users.information.BulkSectionUpdateResponse;
import com.attendease.backend.domain.user.account.management.users.information.BulkStudentSectionUpdateRequest;
import com.attendease.backend.domain.user.account.management.users.information.UserAccountManagementUsersInformationRequest;
import com.attendease.backend.domain.user.account.management.users.information.UserAccountManagementUsersInformationResponse;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.osa.service.management.user.information.ManagementUserInformationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * {@code ManagementUserInformationController} is used for managing user information updates and bulk deletions.
 *
 * <p>This controller provides endpoints for osa (Office of Student Affairs) user to update user details (including student-specific fields)
 * and perform administrative actions like deleting all student and associated data. All endpoints are secured for osa role user only.</p>
 *
 * @author  jakematthewviado204@gmail.com
 * @since 2025-Nov-26
 */
@RestController
@RequestMapping("/api/osa/user/information/management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class ManagementUserInformationController {

    private final ManagementUserInformationService managementUserInformationService;

    /**
     * Permanently deletes all student along with their associated user accounts and facial biometrics data.
     *
     * <p>This endpoint is for administrative cleanup, such as resetting the database during testing or migration.
     * It cascades deletions to linked entities (User, Students, and StudentBiometrics collections).
     * Use with extreme caution as it is irreversible.</p>
     *
     * <p><strong>Response:</strong> HTTP 200 with a confirmation message including the count of deleted student.</p>
     *
     * @return {@link ResponseEntity} with status 200 and a success message (e.g., "Successfully deleted X student.")
     * @see ManagementUserInformationService#deleteAllStudentsAndAssociatedUserAndFacialData()
     */
    @DeleteMapping("/student/remove-all")
    public ResponseEntity<String> deleteAllStudentsAndAssociatedUserAndFacialData() {
        long deletedCount = managementUserInformationService.deleteAllStudentsAndAssociatedUserAndFacialData();
        return ResponseEntity.ok("Successfully deleted " + deletedCount + " student.");
    }

    /**
     * Updates user information for a specified user, including optional student-specific fields if applicable.
     *
     * <p>This endpoint allows osa user to modify user details like name, email, contact, password, and for student:
     * student number or section assignment. Updates propagate to linked entities (e.g., Students via DBRef).
     * The current authenticated osa user's ID is captured as the updater for audit trails.</p>
     *
     * <p><strong>Path Variable:</strong> {@code userId} - the unique ID of the user to update.</p>
     *
     * <p><strong>Request Body:</strong> {@link UserAccountManagementUsersInformationRequest} containing optional fields to update (e.g., firstName, password, sectionId).</p>
     *
     * <p><strong>Response:</strong>
     * <ul>
     * <li>For non-student user: HTTP 200 with the updated {@link User} object.</li>
     * <li>For student user: HTTP 200 with the updated {@link UserStudentResponse} including section/course/cluster details.</li>
     * </ul></p>
     *
     * @param userId the unique ID of the user to update
     * @param request the validated {@link UserAccountManagementUsersInformationRequest} object with fields to update
     * @return {@link ResponseEntity} with status 200 and the updated user or student response
     * @throws ChangeSetPersister.NotFoundException if the user or referenced entities (e.g., section) are not found
     * @throws IllegalArgumentException if validation fails (e.g., invalid password, duplicate student number)
     * @see ManagementUserInformationService#updateUserInfo(String, UserAccountManagementUsersInformationRequest, String)
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo(@PathVariable String userId, @Valid @RequestBody UserAccountManagementUsersInformationRequest request) throws ChangeSetPersister.NotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String updatedByUserId = auth.getName();
        UserAccountManagementUsersInformationResponse result = managementUserInformationService.updateUserInfo(userId, request, updatedByUserId);
        if (result.getUser().getUserType() == UserType.STUDENT) {
            return ResponseEntity.ok(result.getStudentResponse());
        } else {
            return ResponseEntity.ok(result.getUser());
        }
    }

    /**
     * Bulk update students to a specific section
     */
    @PutMapping("/section/bulk")
    public ResponseEntity<?> bulkUpdateStudentSection(@RequestBody BulkStudentSectionUpdateRequest request) {
        int updatedCount = managementUserInformationService.bulkUpdateStudentSection(request);
        return ResponseEntity.ok().body(new BulkSectionUpdateResponse(updatedCount, "Students successfully assigned to section"));
    }
}
