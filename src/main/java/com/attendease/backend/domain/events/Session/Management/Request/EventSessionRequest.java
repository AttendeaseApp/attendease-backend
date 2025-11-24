package com.attendease.backend.domain.events.Session.Management.Request;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EligibleAttendees.EligibilityCriteria;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Request DTO for creating/updating events. Uses IDs for criteria to keep payloads lightweight.
 * Service populates full objects (Clusters, Courses, Sections) via repositories for storage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSessionRequest {

    private String eventName;
    private String description;
    private String eventLocationId;

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

    private EligibilityCriteria eligibleStudents;
}
