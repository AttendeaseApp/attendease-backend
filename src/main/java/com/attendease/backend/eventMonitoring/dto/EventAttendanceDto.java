package com.attendease.backend.eventMonitoring.dto;

import com.attendease.backend.model.enums.AttendanceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EventAttendanceDto {
    private String recordId;
    private String studentNumber;
    private String eventId;
    private String locationId;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;
    private AttendanceStatus attendanceStatus;

    public EventAttendanceDto(String recordId, String studentNumber, String eventId, String locationId, LocalDateTime timeIn, LocalDateTime timeOut, AttendanceStatus attendanceStatus) {
        this.recordId = recordId;
        this.studentNumber = studentNumber;
        this.eventId = eventId;
        this.locationId = locationId;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.attendanceStatus = attendanceStatus;
    }
}
