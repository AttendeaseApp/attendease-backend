package com.attendease.backend.domain.events.Session.Management.Response;

import com.attendease.backend.domain.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for event creation. Includes lightweight eligibility details (IDs + names)
 * for client display. Excludes heavy domain fields like full locations or audit timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreationResponse {

    private String eventId;

    private String eventName;
    private String description;
    private String eventLocationId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeInRegistrationStartDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDateTime;

    private EventStatus eventStatus;

    private boolean allStudents;

    private List<String> sectionIDs;
    private List<String> sectionNames;

    private List<String> courseIDs;
    private List<String> courseNames;

    private List<String> clusterIDs;
    private List<String> clusterNames;
}
