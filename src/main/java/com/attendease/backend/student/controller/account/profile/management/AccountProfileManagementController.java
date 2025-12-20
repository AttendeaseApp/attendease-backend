package com.attendease.backend.student.controller.account.profile.management;

import com.attendease.backend.domain.student.password.update.PasswordUpdateRequest;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.student.service.account.profile.management.AccountProfileManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that handles student profile-related actions such as retrieving
 * student/user information and updating account passwords.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AccountProfileManagementController {

    private final AccountProfileManagementService accountProfileManagementService;

    /**
     * Retrieves the authenticated student's profile with essential information only.
     *
     * <p>This endpoint returns a {@link UserStudentResponse} containing:
     * user details, student info, section/course/cluster data, and biometric status.</p>
     *
     * @param authentication the authentication object containing the authenticated user's ID
     * @return a {@link ResponseEntity} containing:
     * <ul>
     *     <li>{@code 200 OK} and the {@link UserStudentResponse} if found</li>
     *     <li>{@code 404 Not Found} if the user profile is not found</li>
     * </ul>
     */
    @GetMapping("/me")
    public ResponseEntity<UserStudentResponse> getUserProfile(Authentication authentication) {
        String authenticatedUserId = authentication.getName();

        UserStudentResponse profile = accountProfileManagementService.getUserStudentProfile(authenticatedUserId);

        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profile);
    }

    /**
     * Updates the password of the authenticated student.
     *
     * <p>This endpoint validates the old password, checks the new password against
     * password policy rules, and updates the stored password if all conditions are met.</p>
     *
     * @param authentication the authentication object containing the student's ID
     * @param request the password update request body containing:
     *                <ul>
     *                    <li>oldPassword</li>
     *                    <li>newPassword</li>
     *                </ul>
     * @return a {@link ResponseEntity} containing:
     * <ul>
     *     <li>{@code 200 OK} and a success message if the password is updated</li>
     *     <li>{@code 400 Bad Request} if validation fails (handled by service exceptions)</li>
     * </ul>
     */
    @PatchMapping("/update-password")
    public ResponseEntity<String> updatePassword(Authentication authentication, @RequestBody @Valid PasswordUpdateRequest request) {
        String id = authentication.getName();
        String response = accountProfileManagementService.updatePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(response);
    }
}
