package com.attendease.backend.userManagement.dto;

import com.attendease.backend.model.students.Students;
import com.attendease.backend.model.users.Users;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object that combines user information with optional student information.
 * Used to represent the relationship between Users and Students entities.
 */
@Data
@AllArgsConstructor
public class UserWithStudentInfo {
    private final Users user;
    private final Students studentInfo;

    /**
     * Constructor for users without student information
     * @param user The user entity
     */
    public UserWithStudentInfo(Users user) {
        this.user = user;
        this.studentInfo = null;
    }

    /**
     * Checks if this user has associated student information
     * @return true if student information is present, false otherwise
     */
    public boolean hasStudentInfo() {
        return studentInfo != null;
    }

    /**
     * Checks if this is a student user type
     * @return true if user type is STUDENT, false otherwise
     */
    public boolean isStudent() {
        return user != null &&
                user.getUserType() != null &&
                user.getUserType().name().equals("STUDENT");
    }

    /**
     * Gets the user ID
     * @return user ID if user exists, null otherwise
     */
    public String getUserId() {
        return user != null ? user.getUserId() : null;
    }

    /**
     * Gets the student number if available
     * @return student number if student info exists, null otherwise
     */
    public String getStudentNumber() {
        return (studentInfo != null) ? studentInfo.getStudentNumber() : null;
    }
}