package com.attendease.backend.authentication.student.controller;

import com.attendease.backend.authentication.student.service.UserProfileService;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.students.UserStudent.UserStudent;
import com.attendease.backend.domain.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/user-student/me")
    public ResponseEntity<UserStudent> getUserStudentProfile(Authentication authentication) {
        String authenticatedUserId = authentication.getName();

        var userOpt = userProfileService.getUserProfileByUserId(authenticatedUserId);
        var studentOpt = userProfileService.getStudentProfileByUserId(authenticatedUserId);

        if (userOpt.isEmpty() && studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserStudent userStudent = new UserStudent();
        userOpt.ifPresent(userStudent::setUser);
        studentOpt.ifPresent(userStudent::setStudent);

        return ResponseEntity.ok(userStudent);
    }

}
