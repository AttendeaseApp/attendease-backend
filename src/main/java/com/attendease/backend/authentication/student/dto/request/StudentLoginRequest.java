package com.attendease.backend.authentication.student.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentLoginRequest {
    private String studentNumber;
    private String password;

    /**
     * Validates that login credentials are present
     * @return true if both studentNumber and password are provided
     */
    public boolean isValid() {
        return studentNumber != null && !studentNumber.trim().isEmpty() && password != null && !password.trim().isEmpty();
    }
}
