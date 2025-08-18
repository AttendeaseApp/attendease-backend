package com.attendease.backend.data.model.students;

import com.attendease.backend.data.model.users.Users;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class Students extends Users {
    @PropertyName("userRefId")
    private DocumentReference userRefId;

    @PropertyName("facialRefID")
    private DocumentReference facialRefID;

    @PropertyName("studentNumber")
    private String studentNumber;

    @PropertyName("section")
    private String section;

    @PropertyName("yearLevel")
    private String yearLevel;

    @PropertyName("courseRefId")
    private DocumentReference courseRefId;

    @PropertyName("clusterRefId")
    private DocumentReference clusterRefId;

    public Students() {
        super();
    }

    public void logFields() {
        log.info("Students fields: userRefId={}, facialRefID={}, studentNumber={}, section={}, yearLevel={}, courseRefId={}, clusterRefId={}",
                userRefId!= null ? userRefId.getPath() :null,
                facialRefID != null ? facialRefID.getPath() : null,
                studentNumber,
                section ,
                yearLevel,
                courseRefId != null ?courseRefId.getPath() :null,
                clusterRefId != null ?clusterRefId.getPath() :null);
        log.info("Users fields: userId={}, firstName={}, email={}",
                getUserId(), getFirstName(), getEmail());
    }

    @Override
    public String toString() {
        return "Students{" +
                "userRefId=" + (userRefId != null ? userRefId.getPath() : null) +
                ", facialRefID=" + (facialRefID != null ? facialRefID.getPath() : null) +
                ", studentNumber='" + studentNumber + '\'' +
                ", section='" + section + '\'' +
                ", yearLevel='" + yearLevel + '\'' +
                ", courseRefId=" + (courseRefId != null ? courseRefId.getPath() : null) +
                ", clusterRefId=" + (clusterRefId != null ? clusterRefId.getPath() : null) +
                ", parent=" + super.toString() +
                '}';
    }
}