package com.attendease.attendease_backend.data.student;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

@Data
public class Cluster {

    @DocumentId
    private String clusterId;
    private String clusterName;
    private DocumentReference createdByUserRefId;
    private DocumentReference updatedByUserRefId;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;
}
