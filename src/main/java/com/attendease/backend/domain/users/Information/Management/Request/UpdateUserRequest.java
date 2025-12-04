package com.attendease.backend.domain.users.Information.Management.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

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

    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,128}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character, and be 8-128 characters long"
    )
    private String password;

    @Pattern(
            regexp = "^[+]?[0-9]{11,15}$",
            message = "Contact number must be a valid phone number (11-15 digits, optional + prefix; spaces/dashes ignored)"
    )
    private String contactNumber;

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 9, max = 9, message = "Student number must be exactly 9 characters (e.g., CT00-0000)")
    @Pattern(regexp = "^CT\\d{2}-\\d{4}$", message = "Student number must follow format CTyy-xxxx (e.g., CT00-0000)")
    private String studentNumber;

    private String sectionId;
}
