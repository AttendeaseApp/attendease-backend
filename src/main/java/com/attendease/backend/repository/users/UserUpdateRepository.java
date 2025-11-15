package com.attendease.backend.repository.users;

import com.attendease.backend.domain.users.Users;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface UserUpdateRepository {
    Long deleteAllStudentsAndAssociatedUserAndFacialData() throws ExecutionException, InterruptedException;
    Users deactivateUser(String userId) throws ExecutionException, InterruptedException;
    Users reactivateUser(String userId) throws ExecutionException, InterruptedException;
    List<Users> bulkDeactivateUsers() throws ExecutionException, InterruptedException;
    List<Users> searchUsersByKeywords(String keyword);
}
