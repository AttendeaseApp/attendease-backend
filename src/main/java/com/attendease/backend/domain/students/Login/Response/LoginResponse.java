package com.attendease.backend.domain.students.Login.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private boolean requiresFacialRegistration;
    private String message;
    private String studentNumber;
}
