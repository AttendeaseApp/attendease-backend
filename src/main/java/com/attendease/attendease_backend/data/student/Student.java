package com.attendease.attendease_backend.data.student;

import com.attendease.attendease_backend.data.user.User;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Student extends User {

    private DocumentReference userRefId;
    private DocumentReference facialRefID;
    private String studentNumber;
    private String section;
    private String yearLevel;
    private DocumentReference courseRefId;
    private DocumentReference clusterRefId;
}
