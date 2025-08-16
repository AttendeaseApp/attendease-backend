package com.attendease.backend.data.model.students;

import com.attendease.backend.data.model.users.Users;
import com.google.cloud.firestore.DocumentReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Students extends Users {

    private DocumentReference userRefId;
    private DocumentReference facialRefID;
    private String studentNumber;
    private String section;
    private String yearLevel;
    private DocumentReference courseRefId;
    private DocumentReference clusterRefId;
}
