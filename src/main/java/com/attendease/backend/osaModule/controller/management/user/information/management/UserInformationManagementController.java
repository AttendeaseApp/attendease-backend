package com.attendease.backend.osaModule.controller.management.user.information.management;

import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.users.Information.Management.Request.UpdateUserRequest;
import com.attendease.backend.domain.users.Information.Management.Response.UpdateResultResponse;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.osaModule.service.management.user.information.management.UserInformationManagementService;
import jakarta.validation.Valid;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/information/management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class UserInformationManagementController {

    private final UserInformationManagementService userInformationManagementService;

    /**
     * Allows OSA to permanently delete all students along with their associated biometrics data.
     */
    @DeleteMapping("/students")
    public ResponseEntity<String> deleteAllStudentsAndAssociatedUserAndFacialData() throws ExecutionException, InterruptedException {
        long deletedCount = userInformationManagementService.deleteAllStudentsAndAssociatedUserAndFacialData();
        return ResponseEntity.ok("Successfully deleted " + deletedCount + " students.");
    }

    /**
     * Allows OSA to update user information for any user (including student-specific fields if applicable).
     *
     * @param userId The ID of the user to update
     * @param request The update request containing optional fields to update
     * @return The updated user
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest request) throws ChangeSetPersister.NotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String updatedByUserId = auth.getName();
        UpdateResultResponse result = userInformationManagementService.osaUpdateUserInfo(userId, request, updatedByUserId);
        if (result.getUser().getUserType() == UserType.STUDENT) {
            return ResponseEntity.ok(result.getStudentResponse());
        } else {
            return ResponseEntity.ok(result.getUser());
        }
    }
}
