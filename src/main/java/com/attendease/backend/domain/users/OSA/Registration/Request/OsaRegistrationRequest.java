package com.attendease.backend.domain.users.OSA.Registration.Request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsaRegistrationRequest {

    private String firstName;
    private String lastName;
    private String password;
    private String contactNumber;
    private String email;
}


