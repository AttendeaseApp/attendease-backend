package com.attendease.attendease_backend.data.student;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class Cluster extends Course{

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
