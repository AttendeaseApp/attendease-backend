package com.attendease.backend.model.students;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class Courses extends Students {
    @DocumentId
    @PropertyName("courseId")
    private String courseId;

    @PropertyName("courseName")
    private String courseName;

    @PropertyName("clusterRefId")
    private DocumentReference clusterRefId;

    @PropertyName("createdByUserRefId")
    private DocumentReference createdByUserRefId;

    @PropertyName("updatedByUserRefId")
    private DocumentReference updatedByUserRefId;

    @ServerTimestamp
    @PropertyName("createdAt")
    private Date createdAt;

    @ServerTimestamp
    @PropertyName("updatedAt")
    private Date updatedAt;

    public Courses() {
        super();
    }
}