package com.attendease.backend.studentModule.controller.profile;

import com.attendease.backend.domain.students.Password.Update.Request.PasswordUpdateRequest;
import com.attendease.backend.domain.students.UserStudent.UserStudent;
import com.attendease.backend.studentModule.service.profile.StudentProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/user-student/me")
    public ResponseEntity<UserStudent> getUserStudentProfile(Authentication authentication) {
        String authenticatedUserId = authentication.getName();

        var userOpt = studentProfileService.getUserProfileByUserId(authenticatedUserId);
        var studentOpt = studentProfileService.getStudentProfileByUserId(authenticatedUserId);

        if (userOpt.isEmpty() && studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserStudent userStudent = new UserStudent();
        userOpt.ifPresent(userStudent::setUser);
        studentOpt.ifPresent(userStudent::setStudent);

        return ResponseEntity.ok(userStudent);
    }

    /**
     * Update student password
     */
    @PatchMapping("/update-password")
    public ResponseEntity<String> updatePassword(Authentication authentication, @RequestBody @Valid PasswordUpdateRequest request) {
        String id = authentication.getName();
        String response = studentProfileService.updatePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(response);
    }
}
