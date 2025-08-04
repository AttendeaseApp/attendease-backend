package com.attendease.attendease_backend.data.student;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

@Data
public class Student {

    @DocumentId
    private String studentId;
    private DocumentReference userRefId;
    private DocumentReference facialRefID;
    private String studentNumber;
    private String section;
    private String yearLevel;
    private DocumentReference courseRefId;
    private DocumentReference clusterRefId;
}
