package com.attendease.backend.userManagement.service;

import com.attendease.backend.domain.users.Search.SearchKeywords;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.users.UserUpdateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateUsersService {

    private final UserUpdateRepository userUpdateRepository;

    /**
     * Deactivates a single user
     */
    public Users deactivateUserService(String userId) {
        try {
            Users deactivatedUser = userUpdateRepository.deactivateUser(userId);
            log.info("Successfully deactivated user {}", userId);
            return deactivatedUser;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to deactivate user {}", userId, e);
            throw new RuntimeException("Failed to deactivate user: " + e.getMessage(), e);
        }
    }

    /**
     * Deactivates all users with ACTIVE status
     */
    public List<Users> bulkDeactivationOnUsersService() {
        try {
            List<Users> deactivatedUsers = userUpdateRepository.bulkDeactivateUsers();
            log.info("Successfully bulk-deactivated {} users", deactivatedUsers.size());
            return deactivatedUsers;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Bulk deactivation failed", e);
            throw new RuntimeException("Bulk deactivation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Reactivates a user
     */
    public Users reactivateUserService(String userId) {
        try {
            Users reactivatedUser = userUpdateRepository.reactivateUser(userId);
            log.info("Successfully reactivated user {}", userId);
            return reactivatedUser;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to reactivate user {}", userId, e);
            throw new RuntimeException("Failed to reactivate user: " + e.getMessage(), e);
        }
    }

    /**
     * Search users by keywords in name, email, or contact number
     */
    public List<Users> searchUsers(SearchKeywords searchDTO) {
        String keyword = searchDTO.getKeyword();
        List<Users> results = userUpdateRepository.searchUsersByKeywords(keyword);
        log.info("Found {} users for keyword '{}'", results.size(), keyword);
        return results;
    }

    public long deleteAllStudentsAndAssociatedUserAndFacialData() throws ExecutionException, InterruptedException {
        return userUpdateRepository.deleteAllStudentsAndAssociatedUserAndFacialData();
    }
}
