package com.attendease.backend.data.model.students;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class Courses extends Students {

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
