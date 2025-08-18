/**
 * REST controller for managing user-related API endpoints.
 * Provides endpoints for retrieving, updating, deactivating, reactivating, deleting, and searching users and students.
 * This controller interacts with the UserManagementServiceImpl to handle business logic and data operations.
 */

package com.attendease.backend.controller.management.user;

import com.attendease.backend.data.dto.student.StudentDTO;
import com.attendease.backend.data.dto.users.BulkUserOperationsDTO;
import com.attendease.backend.data.dto.users.UpdateUserInfoDTO;
import com.attendease.backend.data.dto.users.UserSearchDTO;
import com.attendease.backend.data.dto.users.UsersDTO;
import com.attendease.backend.services.management.user.impl.UserManagementServiceImpl;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Controller for handling user management API requests under the /v1/api/users endpoint.
 */
@RestController
@RequestMapping("v1/api/users")
@Slf4j
public class UserManagementController {
    private final UserManagementServiceImpl userManagementService;

    /**
     * Constructor for UserManagementController.
     * @param userManagement The service implementation for user management operations.
     */
    public UserManagementController(UserManagementServiceImpl userManagement) {
        this.userManagementService = userManagement;
    }

    /**
     * Retrieves all users (OSA and Students).
     * @return ResponseEntity containing a list of UsersDTO or an empty list with 404 status if no users are found.
     */
    @GetMapping
    public ResponseEntity<List<UsersDTO>> retrieveAllUsers() {
        List<UsersDTO> users = userManagementService.retrieveAllUsersService();
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(users);
    }

    /**
     * Imports students from a CSV file.
     * @param file The CSV file containing student data.
     * @return ResponseEntity containing a list of imported UsersDTO or an error status.
     */
    @PostMapping("/import")
    public ResponseEntity<List<UsersDTO>> importStudents(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                log.error("Uploaded file is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            List<UsersDTO> importedUsers = userManagementService.importStudentsViaCSV(file);
            log.info("Successfully imported {} students", importedUsers.size());
            return ResponseEntity.ok(importedUsers);
        } catch (IOException | CsvValidationException e) {
            log.error("Failed to process CSV file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException | ExecutionException | InterruptedException e) {
            log.error("Failed to import students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves all students.
     * @return ResponseEntity containing a list of StudentDTO or an empty list with 404 status if no students are found.
     */
    @GetMapping("/students")
    public ResponseEntity<List<StudentDTO>> retrieveAllStudents() {
        List<StudentDTO> students = userManagementService.retrieveAllStudentsService();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(students);
    }

    /**
     * Updates user information for a specific user identified by userId.
     * @param userId The ID of the user to update.
     * @param updateDTO Data transfer object containing updated user information.
     * @return ResponseEntity containing the updated UsersDTO or 404 status if the user is not found.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UsersDTO> updateUser(@PathVariable String userId, @RequestBody UpdateUserInfoDTO updateDTO) {
        try {
            UsersDTO updatedUser = userManagementService.updateUserInformationService(userId, updateDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Deactivates a user account identified by userId.
     * @param userId The ID of the user to deactivate.
     * @return ResponseEntity containing the deactivated UsersDTO or 404 status if the user is not found.
     */
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<UsersDTO> deactivateUser(@PathVariable String userId) {
        try {
            UsersDTO userDTO = userManagementService.deactivateUserService(userId);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

   /**
     * Deactivates multiple user accounts based on the provided list of user IDs.
     * @param bulkDTO Data transfer object containing a list of user IDs to deactivate.
     * @return ResponseEntity containing a list of deactivated UsersDTO or 400 status if the operation fails.
     */
    @PatchMapping("/deactivate")
    public ResponseEntity<List<UsersDTO>> deactivateUsers(@RequestBody BulkUserOperationsDTO bulkDTO) {
        try {
            List<UsersDTO> userDTOs = userManagementService.bulkDeactivationOnUsersService(bulkDTO);
            return ResponseEntity.ok(userDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Reactivates a user account identified by userId.
     * @param userId The ID of the user to reactivate.
     * @return ResponseEntity containing the reactivated UsersDTO or 404 status if the user is not found.
     */
    @PatchMapping("/{userId}/reactivate")
    public ResponseEntity<UsersDTO> reactivateUser(@PathVariable String userId) {
        try {
            UsersDTO userDTO = userManagementService.reactivateUserService(userId);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Searches for users based on the provided search criteria.
     * @param searchDTO Data transfer object containing search parameters.
     * @return ResponseEntity containing a list of matching UsersDTO or an empty list with 404 status if no users are found.
     */
    @PostMapping("/search")
    public ResponseEntity<List<UsersDTO>> searchUsers(@RequestBody UserSearchDTO searchDTO) {
        try {
            List<UsersDTO> userDTOs = userManagementService.searchUsers(searchDTO);
            if (userDTOs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }
            return ResponseEntity.ok(userDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
