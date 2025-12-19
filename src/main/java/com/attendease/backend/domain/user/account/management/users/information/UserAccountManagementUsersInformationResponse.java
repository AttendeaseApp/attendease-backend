package com.attendease.backend.domain.user.account.management.users.information;

import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountManagementUsersInformationResponse {
    private User user;
    private UserStudentResponse studentResponse;
}