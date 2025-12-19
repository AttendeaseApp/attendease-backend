package com.attendease.backend.domain.user;

import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Domain entity representing a user account in the Attendease system.
 * <p>
 * Core entity for authentication and personalization, supporting student, or in future other admins.
 * Includes validation for names, credentials, and contact info. Defaults to ACTIVE status.
 * Unique email index; auditing timestamps for compliance.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 * @see com.attendease.backend.domain.enums.UserType
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user")
public class User {

    @Id
    private String userId;

    private String firstName;

    private String lastName;

    private String password;

    @Indexed
    private String contactNumber;

    @Indexed(unique = true)
    private String email;

    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @NotNull
    private UserType userType;

    private String updatedBy;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

