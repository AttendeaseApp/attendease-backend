package com.attendease.backend.osa.controller.profile;

import com.attendease.backend.domain.user.account.osa.password.reset.UserAccountOsaPasswordResetRequest;
import com.attendease.backend.domain.user.account.profile.UserAccountProfile;
import com.attendease.backend.osa.service.profile.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/osa/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class ProfileServiceController {

    private final ProfileService profileService;

    /**
     * Use this to retrieve the profile of an authenticated osa.
     *
     * @return mapped UserAccountProfile object
     * */
    @GetMapping("/user-osa/me")
    public ResponseEntity<UserAccountProfile> getUserStudentProfile(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        return profileService.getOsaProfileByUserId(authenticatedUserId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Used for updating the password of osa itself
     * */
    @PatchMapping("/account/password/update")
    public ResponseEntity<String> updatePassword(Authentication authentication, @Valid @RequestBody UserAccountOsaPasswordResetRequest request) {
        String authenticatedUserId = authentication.getName();
        String result = profileService.updatePassword(authenticatedUserId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(result);
    }

}
