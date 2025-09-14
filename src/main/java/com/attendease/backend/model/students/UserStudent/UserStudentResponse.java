package com.attendease.backend.model.students.UserStudent;

import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.enums.UserType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserStudentResponse {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private AccountStatus accountStatus;
    private UserType userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String studentId;
    private String studentNumber;
    private String section;
    private String yearLevel;
    private boolean active;
}
