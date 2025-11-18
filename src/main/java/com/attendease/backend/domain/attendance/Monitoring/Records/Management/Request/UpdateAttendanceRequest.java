package com.attendease.backend.domain.attendance.Monitoring.Records.Management.Request;

import com.attendease.backend.domain.enums.AttendanceStatus;
import lombok.Data;

@Data
public class UpdateAttendanceRequest {

    private AttendanceStatus status;
    private String reason;
}
