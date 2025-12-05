package com.attendease.backend.osa.controller.management.user.information;

import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.users.Information.Management.Request.UpdateUserRequest;
import com.attendease.backend.domain.users.Information.Management.Response.UpdateResultResponse;
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
 * <p>This controller provides endpoints for OSA (Office of Student Affairs) users to update user details (including student-specific fields)
 * and perform administrative actions like deleting all students and associated data. All endpoints are secured for OSA role users only.</p>
 *
 * @author  jakematthewviado204@gmail.com
 * @since 2025-Nov-26
 */
@RestController
@RequestMapping("/api/users/information/management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class ManagementUserInformationController {

    private final ManagementUserInformationService managementUserInformationService;

    /**
     * Permanently deletes all students along with their associated user accounts and facial biometrics data.
     *
     * <p>This endpoint is for administrative cleanup, such as resetting the database during testing or migration.
     * It cascades deletions to linked entities (Users, Students, and StudentBiometrics collections).
     * Use with extreme caution as it is irreversible.</p>
     *
     * <p><strong>Response:</strong> HTTP 200 with a confirmation message including the count of deleted students.</p>
     *
     * @return {@link ResponseEntity} with status 200 and a success message (e.g., "Successfully deleted X students.")
     * @see ManagementUserInformationService#deleteAllStudentsAndAssociatedUserAndFacialData()
     */
    @DeleteMapping("/students/remove-all")
    public ResponseEntity<String> deleteAllStudentsAndAssociatedUserAndFacialData() {
        long deletedCount = managementUserInformationService.deleteAllStudentsAndAssociatedUserAndFacialData();
        return ResponseEntity.ok("Successfully deleted " + deletedCount + " students.");
    }

    /**
     * Updates user information for a specified user, including optional student-specific fields if applicable.
     *
     * <p>This endpoint allows OSA users to modify user details like name, email, contact, password, and for students:
     * student number or section assignment. Updates propagate to linked entities (e.g., Students via DBRef).
     * The current authenticated OSA user's ID is captured as the updater for audit trails.</p>
     *
     * <p><strong>Path Variable:</strong> {@code userId} - the unique ID of the user to update.</p>
     *
     * <p><strong>Request Body:</strong> {@link UpdateUserRequest} containing optional fields to update (e.g., firstName, password, sectionId).</p>
     *
     * <p><strong>Response:</strong>
     * <ul>
     * <li>For non-student users: HTTP 200 with the updated {@link com.attendease.backend.domain.users.Users} object.</li>
     * <li>For student users: HTTP 200 with the updated {@link com.attendease.backend.domain.students.UserStudent.UserStudentResponse} including section/course/cluster details.</li>
     * </ul></p>
     *
     * @param userId the unique ID of the user to update
     * @param request the validated {@link UpdateUserRequest} object with fields to update
     * @return {@link ResponseEntity} with status 200 and the updated user or student response
     * @throws ChangeSetPersister.NotFoundException if the user or referenced entities (e.g., section) are not found
     * @throws IllegalArgumentException if validation fails (e.g., invalid password, duplicate student number)
     * @see ManagementUserInformationService#osaUpdateUserInfo(String, UpdateUserRequest, String)
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest request) throws ChangeSetPersister.NotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String updatedByUserId = auth.getName();
        UpdateResultResponse result = managementUserInformationService.osaUpdateUserInfo(userId, request, updatedByUserId);
        if (result.getUser().getUserType() == UserType.STUDENT) {
            return ResponseEntity.ok(result.getStudentResponse());
        } else {
            return ResponseEntity.ok(result.getUser());
        }
    }
}
