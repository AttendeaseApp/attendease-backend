package com.attendease.backend.model.students;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

/**
 * Class representing a student cluster.
 */
@Data
public class Clusters {

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
