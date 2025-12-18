package com.attendease.backend.domain.students.UserStudent;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.user.User;
import lombok.Data;

@Data
public class UserStudent {
    private User user;
    private Students student;
}
