package com.attendease.backend.domain.students.UserStudent;

import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String studentId;
    private String studentNumber;
    private String section;
    private String course;
    private String cluster;
}
