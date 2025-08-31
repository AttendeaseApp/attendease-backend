package com.attendease.backend.model.users;

import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.enums.UserType;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

/**
 * Class representing a user.
 */
@Data
public class Users {

    @DocumentId
    private String userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String password;
    private Date birthdate;
    private String address;
    private String contactNumber;
    private String email;

    private AccountStatus accountStatus;
    private UserType userType;
    private UserType updatedBy;

    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;


}
