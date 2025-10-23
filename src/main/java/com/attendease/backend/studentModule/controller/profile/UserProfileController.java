package com.attendease.backend.studentModule.controller.profile;

import com.attendease.backend.studentModule.service.profile.StudentProfileService;
import com.attendease.backend.domain.students.UserStudent.UserStudent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

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

}
