package com.attendease.backend.domain.user.account.management.users.csv.row;

import lombok.Data;

@Data
public class UserAccountManagementUsersCSVRowData {

    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private String studentNumber;
    private String sectionName;
    private String contactNumber;
}
