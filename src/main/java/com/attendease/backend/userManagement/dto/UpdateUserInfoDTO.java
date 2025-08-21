package com.attendease.backend.userManagement.dto;

import com.attendease.backend.model.enums.AccountStatus;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateUserInfoDTO {
    private String firstName;
    private String middleName;
    private String lastName;
    private Date birthdate;
    private String address;
    private String contactNumber;
    private String email;
    private AccountStatus accountStatus;

    // student field (optional, only used if userType is STUDENT)
    private String studentNumber;
    private String section;
    private String yearLevel;
    private String courseRefId;
}
