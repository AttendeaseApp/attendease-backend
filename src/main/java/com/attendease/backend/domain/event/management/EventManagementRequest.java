package com.attendease.backend.domain.event.management;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Request DTO for creating/updating events. Uses IDs for criteria to keep payloads lightweight.
 * Supports separate location IDs for registration area and event venue. Service populates full objects
 * (e.g., Locations, Clusters, Courses, Sections) via repositories for storage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class EventManagementRequest {

    @NotNull(message = "The event name field is required")
    private String eventName;

    @NotNull(message = "The event description field is required")
    @Size(max = 100000, message = "The event description must not exceed 10000 characters")
    private String description;

    private String academicYearId;

    @NotNull(message = "A location for registration is required")
    private String registrationLocationId;

    @NotNull(message = "A location event venue is required")
    private String venueLocationId;

    @NotNull(message = "The Registration Date & Time field is required")
    @Indexed
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDateTime;

    @NotNull(message = "The Starting Date & Time field is required")
    @Indexed
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startingDateTime;

    @NotNull(message = "The Ending Date & Time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endingDateTime;

    private EventStatus eventStatus;
    private EventEligibility eligibleStudents;

    @JsonProperty("facialVerificationEnabled")
    private Boolean facialVerificationEnabled;

    @JsonProperty("attendanceLocationMonitoringEnabled")
    private Boolean attendanceLocationMonitoringEnabled;

    @JsonProperty("strictLocationValidation")
    private Boolean strictLocationValidation;
}