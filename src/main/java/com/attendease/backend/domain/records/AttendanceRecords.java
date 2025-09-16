package com.attendease.backend.domain.records;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.students.Students;
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

import java.time.LocalDateTime;

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

    @NotNull(message = "Time in is required")
    private LocalDateTime timeIn;

    private LocalDateTime timeOut;

    private String reason;

    @Builder.Default
    private AttendanceStatus attendanceStatus = AttendanceStatus.PRESENT;

    private String updatedByUserId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
