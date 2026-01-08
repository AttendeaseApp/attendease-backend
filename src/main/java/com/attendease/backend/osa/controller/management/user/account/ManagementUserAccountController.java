package com.attendease.backend.osa.controller.management.user.account;

import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.exceptions.domain.ImportException.CsvImportException;
import com.attendease.backend.osa.service.management.user.account.ManagementUserAccountService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code ManagementUserAccountController} is used for managing user accounts, including imports, retrievals, and deletions.
 *
 * <p>This controller provides endpoints for osa (Office of Student Affairs) user to handle bulk operations on student accounts,
 * retrieve user/student data, and perform targeted deletions. All endpoints are secured for osa role user only.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-06
 */
@RestController
@RequestMapping("/api/user/management")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class ManagementUserAccountController {

    private final ManagementUserAccountService managementUserAccountService;

    /**
     * Retrieves all user (osa and student) with their associated student details.
     *
     * <p>This endpoint fetches a combined view of user and student, returning enriched responses for student
     * (including section, course, cluster hierarchies). Returns an empty list with 404 status if no user exist.</p>
     *
     * <p><strong>Response:</strong> List of {@link UserStudentResponse} objects.</p>
     *
     * @return {@link ResponseEntity} with status 200 and the list, or 404 with empty list if none found
     * @see ManagementUserAccountService#retrieveUsers()
     */
    @GetMapping
    public ResponseEntity<List<UserStudentResponse>> retrieveAllUsers() {
        List<UserStudentResponse> users = managementUserAccountService.retrieveUsers();
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/students/active")
    public List<UserStudentResponse> activeStudents() {
        return managementUserAccountService.retrieveActiveStudents();
    }

    @GetMapping("/students/inactive")
    public List<UserStudentResponse> inactiveStudents() {
        return managementUserAccountService.retrieveInactiveStudents();
    }

    @PostMapping("/students/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkActivate(@RequestBody List<String> userIds) {
        managementUserAccountService.bulkActivateStudents(userIds);
    }

    @PostMapping("/students/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkDeactivate(@RequestBody List<String> userIds) {
        managementUserAccountService.bulkDeactivateStudents(userIds);
    }

    /**
     * Imports student from an uploaded CSV file, creating new user accounts and student entities.
     *
     * <p>This endpoint processes the CSV for bulk student onboarding, validating structure and content.
     * Returns the list of successfully imported user; errors (e.g., duplicates, invalid rows) are handled via exceptions.</p>
     *
     * <p><strong>Request Parameter:</strong> {@code file} - the {@link MultipartFile} CSV upload (required columns: firstName, lastName, studentNumber, password).</p>
     *
     * <p><strong>Response:</strong> HTTP 200 with a list of created {@link User} objects.</p>
     *
     * @param file the CSV {@link MultipartFile} to import
     * @return {@link ResponseEntity} with status 200 and the list of imported user
     * @throws IllegalArgumentException if the file is invalid (e.g., not CSV, missing headers)
     * @throws CsvImportException if import has row-specific errors
     * @see ManagementUserAccountService#importStudentsViaCSV(MultipartFile)
     */
    @PostMapping("/import")
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file) {
        List<User> importedUsers = managementUserAccountService.importStudentsViaCSV(file);
        return ResponseEntity.ok(importedUsers);
    }

    /**
     * Retrieves all student entities.
     *
     * <p>This endpoint provides a dedicated view of all student, excluding non-student user.
     * Returns an empty list with 404 status if no student exist.</p>
     *
     * <p><strong>Response:</strong> List of {@link Students} objects.</p>
     *
     * @return {@link ResponseEntity} with status 200 and the list, or 404 with empty list if none found
     * @see ManagementUserAccountService#retrieveAllStudents()
     */
    @GetMapping("/student")
    public ResponseEntity<List<UserStudentResponse>> retrieveAllStudents() {
        List<UserStudentResponse> students = managementUserAccountService.retrieveAllStudents();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(students);
    }

    /**
     * Permanently deletes a user account by its unique identifier.
     *
     * <p>This endpoint allows administrative removal of any user (osa or student), cascading to linked entities.
     * Intended for targeted cleanups.</p>
     *
     * <p><strong>Path Variable:</strong> {@code userId} - the unique ID of the user to delete.</p>
     *
     * <p><strong>Response:</strong> HTTP 204 No Content on success.</p>
     *
     * @param userId the unique ID of the user to delete
     * @throws ResponseStatusException with 404 status if the user is not found
     * @see ManagementUserAccountService#deleteUserById(String)
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String userId) {
        managementUserAccountService.deleteUserById(userId);
    }

    /**
     * Permanently deletes all student accounts associated with a specific section.
     *
     * <p>This endpoint performs bulk deletion of student (and their user accounts) linked to the given section.
     * Validates section format and existence before cascading deletions.</p>
     *
     * <p><strong>Path Variable:</strong> {@code sectionName} - the name of the section (e.g., "BSIT-401").</p>
     *
     * <p><strong>Response:</strong> HTTP 204 No Content on success.</p>
     *
     * @param sectionName the name of the section to target for deletion
     * @return {@link ResponseEntity} with 204 status
     * @throws ResponseStatusException with 404 status if the section is not found
     * @see ManagementUserAccountService#deleteStudentsBySection(String)
     */
    @DeleteMapping("/section/{sectionName}")
    public ResponseEntity<?> deleteStudentsBySection(@PathVariable String sectionName) {
        managementUserAccountService.deleteStudentsBySection(sectionName);
        return ResponseEntity.noContent().build();
    }
}
