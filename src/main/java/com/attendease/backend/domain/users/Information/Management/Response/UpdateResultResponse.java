package com.attendease.backend.domain.users.Information.Management.Response;

import com.attendease.backend.domain.students.UserStudent.UserStudentResponse;
import com.attendease.backend.domain.users.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateResultResponse {
    private Users user;
    private UserStudentResponse studentResponse;
}