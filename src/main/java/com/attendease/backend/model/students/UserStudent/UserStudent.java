package com.attendease.backend.model.students.UserStudent;

import com.attendease.backend.model.students.Students;
import com.attendease.backend.model.users.Users;
import lombok.Data;

@Data
public class UserStudent {
    private Users user;
    private Students student;
}
