package com.attendease.backend.domain.events;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EligibleAttendees.EligibilityCriteria;
import com.attendease.backend.domain.locations.EventLocations;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Class representing an event session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event_sessions")
public class EventSessions {

    @Id
    private String id;

    @NotBlank(message = "Event name is required")
    @Indexed
    private String eventName;

    @DBRef
    private EventLocations eventLocation;

    private String description;

    @Field("eligibleStudents")
    private EligibilityCriteria eligibleStudents;

    @NotNull(message = "Start date time is required")
    @Indexed
    private Date startDateTime;

    @NotNull(message = "End date time is required")
    private Date endDateTime;

    private EventStatus eventStatus;

    private String createdByUserId;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;
}
