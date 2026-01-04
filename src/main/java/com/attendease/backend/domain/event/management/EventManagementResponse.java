package com.attendease.backend.domain.event.management;

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
public final class EventManagementResponse {

    private String eventId;
    private String eventName;
    private String description;

    private String registrationLocationId;
    private String registrationLocationName;

    private String venueLocationId;
    private String venueLocationName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startingDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endingDateTime;

    private String academicYearId;
    private String academicYearName;
    private Integer semester;
    private String semesterName;

    private EventStatus eventStatus;
    private boolean allStudents;
    private List<Integer> targetYearLevels;
    private List<String> sectionIDs;
    private List<String> sectionNames;
    private List<String> courseIDs;
    private List<String> courseNames;
    private List<String> clusterIDs;
    private List<String> clusterNames;
    private Boolean facialVerificationEnabled;
    private Boolean attendanceLocationMonitoringEnabled;
}
