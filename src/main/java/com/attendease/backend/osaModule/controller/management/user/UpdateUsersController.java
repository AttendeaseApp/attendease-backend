package com.attendease.backend.osaModule.controller.management.user;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.ResetPassword.OsaResetPasswordRequest;
import com.attendease.backend.domain.users.Search.SearchKeywords;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.osaModule.service.management.user.UpdateUsersService;
import com.attendease.backend.osaModule.service.management.user.UsersManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users/management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class UpdateUsersController {

    private final UpdateUsersService updateUsersService;
    private final UsersManagementService usersManagementService;

    /**
     * Deactivate a user by userId
     */
    @PostMapping("/deactivate/{userId}")
    public ResponseEntity<Users> deactivateUser(@PathVariable String userId) {
        Users deactivatedUser = updateUsersService.deactivateUserService(userId);
        return ResponseEntity.ok(deactivatedUser);
    }

    /**
     * Bulk deactivate all active users
     */
    @PostMapping("/deactivate/bulk")
    public ResponseEntity<List<Users>> bulkDeactivateUsers() {
        List<Users> deactivatedUsers = updateUsersService.bulkDeactivationOnUsersService();
        return ResponseEntity.ok(deactivatedUsers);
    }

    /**
     * Reactivate a user by userId
     */
    @PostMapping("/reactivate/{userId}")
    public ResponseEntity<Users> reactivateUser(@PathVariable String userId) {
        Users reactivatedUser = updateUsersService.reactivateUserService(userId);
        return ResponseEntity.ok(reactivatedUser);
    }

    /**
     * Search users by keyword in request body
     */
    @PostMapping("/search")
    public ResponseEntity<List<Users>> searchUsers(@RequestBody SearchKeywords searchDTO) {
        List<Users> users = updateUsersService.searchUsers(searchDTO);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/students")
    public ResponseEntity<String> deleteAllStudentsAndAssociatedUserAndFacialData() throws ExecutionException, InterruptedException {
        long deletedCount = updateUsersService.deleteAllStudentsAndAssociatedUserAndFacialData();
        return ResponseEntity.ok("Successfully deleted " + deletedCount + " students.");
    }

    /**
     *  Delete any user by id
     * */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String userId) throws Exception {
        usersManagementService.deleteUserById(userId);
    }

    /**
     * Resets the password of any users
     * */
    @PutMapping("/osa/reset-password/{userId}")
    public ResponseEntity<Map<String, Object>> osaResetPassword(@PathVariable("userId") String userId, @RequestBody OsaResetPasswordRequest requestBody) {
        String message = updateUsersService.osaResetUserPassword(userId, requestBody.getNewPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}
