/*
 * Data Transfer Object (DTO) used for filtering and searching users.
 * Contains fields to specify search criteria such as search term, user type, account status,
 * student section, year level, and course reference ID.
 */
package com.attendease.backend.data.dto.users;

import com.attendease.backend.data.model.enums.AccountStatus;
import com.attendease.backend.data.model.enums.UserType;
import lombok.Data;

/*
 * @variable searchTerm For full-text search on firstName, lastName, email, studentNumber
 * @variable userType Filter by user type (STUDENT, OSA)
 * @variable accountStatus Filter by account status
 * @variable section Filter by student section
 * @variable yearLevel Filter by student year level
 * @variable courseRefId Filter by course
 */
@Data
public class UserSearchDTO {
    private String searchTerm;
    private UserType userType;
    private AccountStatus accountStatus;
    private String section;
    private String yearLevel;
    private String courseRefId;
}