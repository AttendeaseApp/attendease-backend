package com.attendease.attendease_backend.data.student;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

@Data
public class Course {

    @DocumentId
    private String courseId;
    private String courseName;
    private DocumentReference clusterRefId;
    private DocumentReference createdByUserRefId;
    private DocumentReference updatedByUserRefId;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;
}
