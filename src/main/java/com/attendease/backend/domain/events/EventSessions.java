package com.attendease.backend.domain.events;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EligibleAttendees.EligibilityCriteria;
import com.attendease.backend.domain.locations.EventLocations;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Domain entity representing a scheduled event session.
 * <p>
 * Captures event details like timing, location, and eligibility rules for student registration/attendance.
 * Supports states (e.g., UPCOMING, ONGOING) and geofencing verification. {@link EligibilityCriteria} defines
 * target student (e.g., by section/course/cluster). Post-event, triggers attendance finalization.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event_sessions")
public class EventSessions {

    @Id
    private String eventId;

    @NotBlank(message = "Event name is required")
    @Indexed
    private String eventName;

    @DBRef
    private EventLocations eventLocation;

    @Field("eventLocationId")
    @Indexed
    private String eventLocationId;

    private String description;

    @Field("eligibleStudents")
    private EligibilityCriteria eligibleStudents;

    @NotNull(message = "Time in registration date time is required")
    @Indexed
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeInRegistrationStartDateTime;

    @NotNull(message = "Start date time is required")
    @Indexed
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDateTime;

    private EventStatus eventStatus;

    @Field("facialVerificationEnabled")
    @Builder.Default
    private Boolean facialVerificationEnabled = true;

    private String createdByUserId;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
