package com.attendease.backend.userManagement.controller;

import com.attendease.backend.model.users.Search.SearchKeywords;
import com.attendease.backend.model.users.Users;
import com.attendease.backend.userManagement.service.UpdateUsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/management")
@RequiredArgsConstructor
public class UpdateUsersController {

    private final UpdateUsersService updateUsersService;

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
}
