package com.attendease.backend.domain.events.Session.Management.Request;

import com.attendease.backend.domain.enums.EventStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating events, using IDs for criteria (service populates full objects).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSessionRequest {

    private String eventName;
    private String description;
    private String eventLocationId;
    private LocalDateTime timeInRegistrationStartDateTime;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private EventStatus eventStatus;

    private Boolean allStudents;
    private List<String> clusterIds;
    private List<String> courseIds;
    private List<String> sectionIds;
}
