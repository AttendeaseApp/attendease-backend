package com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response;

import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 *  Used for retrieving attendance records of the student
 * */
@Data
@Builder
public class AttendeesResponse {

    private String attendanceRecordId;
    private String userId;
    private String firstName;
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeIn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeOut;

    private AttendanceStatus attendanceStatus;
    private String email;
    private String contactNumber;
    private AccountStatus accountStatus;
    private String reason;
    private UserType userType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String studentId;
    private String studentNumber;
    
    private String sectionName;
    private String courseName;
    private String clusterName;
}
