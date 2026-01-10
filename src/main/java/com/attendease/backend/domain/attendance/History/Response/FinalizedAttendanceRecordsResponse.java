package com.attendease.backend.domain.attendance.History.Response;

import com.attendease.backend.domain.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO representing a an event attendance records basic records.
 */
@Data
@Builder
public class FinalizedAttendanceRecordsResponse {

    private String eventId;
    private String eventName;

    private String registrationLocationName;
    private String venueLocationName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startingDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endingDateTime;

    private EventStatus eventStatus;

    private int totalPresent;
    private int totalAbsent;
    private int totalIdle;
    private int totalLate;
}
