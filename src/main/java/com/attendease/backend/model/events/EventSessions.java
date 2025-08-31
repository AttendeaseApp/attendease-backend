package com.attendease.backend.model.events;

import com.attendease.backend.model.enums.EventStatus;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Class representing an event session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSessions {

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
