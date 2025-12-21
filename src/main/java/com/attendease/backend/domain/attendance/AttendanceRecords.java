package com.attendease.backend.domain.attendance;

import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.location.Location;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.schedulers.utils.attendance.records.AttendanceRecordsFinalizer;
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
 * Domain entity representing a student's attendance record for a specific event.
 * <p>
 * Tracks check-in/out times, status (e.g., PRESENT, ABSENT), reasons, and ping logs for geofence validation.
 * Finalized post-event via ratio calculations (e.g., 70% inside time = PRESENT). Compound unique index
 * on student+event prevents duplicates. Supports late detection and partial attendance (IDLE).
 * </p>
 * <p><b>Usage Notes:</b> Ping logs appended during ONGOING events. Use finalizer service after endDateTime.
 * Indexes on student/event for efficient queries.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 * @see AttendanceRecordsFinalizer
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
    private Location location;

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
