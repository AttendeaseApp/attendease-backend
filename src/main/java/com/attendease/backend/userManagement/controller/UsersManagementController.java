package com.attendease.backend.userManagement.controller;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.students.UserStudent.UserStudentResponse;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.userManagement.service.UsersManagementService;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
}
