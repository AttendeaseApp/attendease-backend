package com.attendease.attendease_backend.data.event;

import com.attendease.attendease_backend.enums.EventStatus;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

@Data
public class Event {

    @DocumentId
    private String eventId;
    private String eventName;
    private DocumentReference eventLocationRefId;
    private String description;
    private String eligibleStudents;
    private String academicTerm;
    private Date startDateTime;
    private Date endDateTime;
    private EventStatus eventStatus;
    private DocumentReference createdByUserRefId;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;
}
