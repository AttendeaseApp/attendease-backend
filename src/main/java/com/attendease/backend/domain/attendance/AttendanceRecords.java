package com.attendease.backend.domain.attendance;

import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.students.Students;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing attendance records for students at events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "attendance_records")
@CompoundIndex(def = "{'student': 1, 'event': 1}", unique = true) // prevents duplicate attendance
public class AttendanceRecords {

    @Id
    private String recordId;

    @DBRef
    @NotNull(message = "Student reference is required")
    @Indexed
    private Students student;

    @DBRef
    @NotNull(message = "Event reference is required")
    @Indexed
    private EventSessions event;

    @DBRef
    private EventLocations location;

    @Field("eventLocationId")
    @Indexed
    private String eventLocationId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeIn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeOut;

    private String reason;

    private AttendanceStatus attendanceStatus;

    @Builder.Default
    private List<AttendanceTrackingResponse> attendancePingLogs = new ArrayList<>();

    private String updatedByUserId;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
