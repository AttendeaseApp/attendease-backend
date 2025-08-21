package com.attendease.backend.data.dto.users;

import com.attendease.backend.data.model.enums.AccountStatus;
import com.attendease.backend.data.model.enums.UserType;
import com.attendease.backend.data.model.students.Students;
import com.attendease.backend.data.model.users.Users;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Data
@Slf4j
public class UsersDTO {

    // users data fields
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
    private Date createdAt;
    private Date updatedAt;

    // student data fields
    private String studentNumber;
    private String section;
    private String yearLevel;
    private String userRefId; // documentReference
    private String facialRefID; // documentReference
    private String courseRefId; // documentReference
    private String clusterRefId; // documentReference

    public UsersDTO() {}

    public UsersDTO(Users user) {
        this.userId = user.getUserId();
        this.firstName = user.getFirstName();
        this.middleName = user.getMiddleName();
        this.lastName = user.getLastName();
        this.password = null;
        this.birthdate = user.getBirthdate();
        this.address = user.getAddress();
        this.contactNumber = user.getContactNumber();
        this.email = user.getEmail();
        this.accountStatus = user.getAccountStatus();
        this.userType = user.getUserType();
        this.updatedBy = user.getUpdatedBy();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        log.info("Mapped Users to UserDTO: {}", this);
    }

    // users and students
    public UsersDTO(Users user, Students student) {
        this(user);
        if (student != null) {
            this.studentNumber = student.getStudentNumber();
            this.section = student.getSection();
            this.yearLevel = student.getYearLevel();
            this.userRefId = student.getUserRefId() != null ? student.getUserRefId().getPath() : null;
            this.facialRefID = student.getFacialRefID() != null ? student.getFacialRefID().getPath() : null;
            this.courseRefId = student.getCourseRefId() != null ? student.getCourseRefId().getPath() : null;
            this.clusterRefId = student.getClusterRefId() != null ? student.getClusterRefId().getPath() : null;
        }
        log.info("Mapped Users and Students to UserDTO: {}", this);
    }


    @Override
    public String toString() {
        return "UserDTO{" +
                "userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", birthdate=" + birthdate +
                ", address='" + address + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", email='" + email + '\'' +
                ", accountStatus=" + accountStatus +
                ", userType=" + userType +
                ", updatedBy=" + updatedBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", studentNumber='" + studentNumber + '\'' +
                ", section='" + section + '\'' +
                ", yearLevel='" + yearLevel + '\'' +
                ", userRefId='" + userRefId + '\'' +
                ", facialRefID='" + facialRefID + '\'' +
                ", courseRefId='" + courseRefId + '\'' +
                ", clusterRefId='" + clusterRefId + '\'' +
                '}';
    }
}
