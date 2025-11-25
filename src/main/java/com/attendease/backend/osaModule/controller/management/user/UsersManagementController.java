package com.attendease.backend.osaModule.controller.management.user;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.students.UserStudent.UserStudentResponse;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.osaModule.service.management.user.UsersManagementService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/management")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class UsersManagementController {

    private final UsersManagementService userManagementService;

    /**
     * Retrieves all users (OSA and Students).
     */
    @GetMapping
    public ResponseEntity<List<UserStudentResponse>> retrieveAllUsers() {
        List<UserStudentResponse> users = userManagementService.retrieveUsersWithStudents();
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(users);
    }

    /**
     * Imports students from a CSV file.
     */
    @PostMapping("/import")
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file) {
        List<Users> importedUsers = userManagementService.importStudentsViaCSV(file);
        return ResponseEntity.ok(importedUsers);
    }

    /**
     * Retrieves all students only.
     */
    @GetMapping("/students")
    public ResponseEntity<List<Students>> retrieveAllStudents() {
        List<Students> students = userManagementService.retrieveAllStudent();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(students);
    }

    /**
     * Allows OSA to permanently delete any user using their id.
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String userId) throws Exception {
        userManagementService.deleteUserById(userId);
    }

    /**
     * Allows OSA to permanently delete all student accounts associated with a specific section.
     */
    @DeleteMapping("/section/{sectionName}")
    public ResponseEntity<?> deleteStudentsBySection(@PathVariable String sectionName) {
        userManagementService.deleteStudentsBySection(sectionName);
        return ResponseEntity.noContent().build();
    }
}
