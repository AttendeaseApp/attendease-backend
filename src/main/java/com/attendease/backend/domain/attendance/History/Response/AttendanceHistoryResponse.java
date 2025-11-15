package com.attendease.backend.domain.attendance.History.Response;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceHistoryResponse {

    private String eventId;
    private String eventName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeIn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeOut;

    private AttendanceStatus attendanceStatus;

    private String reason;
}
