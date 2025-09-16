package com.attendease.backend.domain.students.UserStudent;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import lombok.Data;

@Data
public class UserStudent {
    private Users user;
    private Students student;
}
