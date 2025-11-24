package com.attendease.backend.domain.users.Information.Management;

import com.attendease.backend.domain.enums.AccountStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$", message = "First name contains invalid characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$", message = "Last name contains invalid characters")
    private String lastName;

    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    private String password;

    private String contactNumber;

    @Email(message = "Email should be valid")
    private String email;

    private AccountStatus accountStatus;

    @Size(min = 8, max = 8, message = "Student number must be exactly 8 characters (e.g., CT00-0000)")
    @Pattern(regexp = "^CT\\d{2}-\\d{4}$", message = "Student number must follow format CTyy-xxxx (e.g., CT00-0000)")
    private String studentNumber;

    private String sectionId;
}
