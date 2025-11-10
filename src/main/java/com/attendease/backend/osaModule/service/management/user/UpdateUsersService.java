package com.attendease.backend.osaModule.service.management.user;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Search.SearchKeywords;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.repository.users.UserUpdateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateUsersService {

    private final UserUpdateRepository userUpdateRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * Allows OSA to reset any user's password without requiring the old password.
     *
     * @param targetUserId The ID of the user whose password is being reset
     * @param newPassword  The new password to assign
     * @return Success message
     */
    public String osaResetUserPassword(String targetUserId, String newPassword) {
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }

        validatePassword(newPassword);

        Users targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OSA"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to reset passwords.");
        }

        targetUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(targetUser);

        log.info("OSA successfully reset password for user {}", targetUserId);
        return "Password reset successfully for user " + targetUserId;
    }


    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password cannot exceed 128 characters");
        }
        if (!Pattern.compile("[A-Za-z]").matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }
    }
}
