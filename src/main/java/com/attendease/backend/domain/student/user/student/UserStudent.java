package com.attendease.backend.domain.student.user.student;

import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import lombok.Data;

@Data
public class UserStudent {
    private User user;
    private Students student;
}
