package com.attendease.backend.student.controller.account.profile.management;

import com.attendease.backend.domain.student.password.update.PasswordUpdateRequest;
import com.attendease.backend.domain.student.user.student.UserStudent;
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
     * Retrieves the authenticated student's combined user and student profile details.
     *
     * <p>This endpoint returns a {@link UserStudent} object, which contains both
     * the associated {@code User} entity and the related {@code Students} entity.
     * Only the fields found for the authenticated user are populated.</p>
     *
     * @param authentication the authentication object containing the authenticated user's ID
     * @return a {@link ResponseEntity} containing:
     * <ul>
     *     <li>{@code 200 OK} and the {@link UserStudent} profile if found</li>
     *     <li>{@code 404 Not Found} if both the user and student records are missing</li>
     * </ul>
     */
    @GetMapping("/user-student/me")
    public ResponseEntity<UserStudent> getUserStudentProfile(Authentication authentication) {
        String authenticatedUserId = authentication.getName();

        var userOpt = accountProfileManagementService.getUserProfileByUserId(authenticatedUserId);
        var studentOpt = accountProfileManagementService.getStudentProfileByUserId(authenticatedUserId);

        if (userOpt.isEmpty() && studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserStudent userStudent = new UserStudent();
        userOpt.ifPresent(userStudent::setUser);
        studentOpt.ifPresent(userStudent::setStudent);

        return ResponseEntity.ok(userStudent);
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
