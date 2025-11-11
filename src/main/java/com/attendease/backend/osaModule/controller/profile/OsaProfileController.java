package com.attendease.backend.osaModule.controller.profile;

import com.attendease.backend.domain.users.Profiles.OsaAccountPasswordUpdateRequest;
import com.attendease.backend.domain.users.Profiles.Profile;
import com.attendease.backend.osaModule.service.profile.OsaProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class OsaProfileController {

    private final OsaProfileService profileService;

    /**
     * Use this to retrieve the profile of an authenticated OSA.
     *
     * @return mapped Profile object
     * */
    @GetMapping("/user-osa/me")
    public ResponseEntity<Profile> getUserStudentProfile(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        return profileService.getOsaProfileByUserId(authenticatedUserId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Used for updating the password of OSA itself
     * */
    @PatchMapping("/account/password/update")
    public ResponseEntity<String> updatePassword(Authentication authentication, @Valid @RequestBody OsaAccountPasswordUpdateRequest request) {
        String authenticatedUserId = authentication.getName();
        String result = profileService.updatePassword(authenticatedUserId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(result);
    }

}
