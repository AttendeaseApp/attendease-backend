package com.attendease.backend.domain.user.account.osa.registration;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountOsaRegistrationRequest {

    private String firstName;
    private String lastName;
    private String password;
    private String contactNumber;
    private String email;
}


