package com.attendease.backend.model.records;

import com.attendease.backend.model.enums.AttendanceStatus;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

/**
 * Class representing attendance records for students at events.
 */
@Data
public class AttendanceRecords {

    @DocumentId
    private String recordId;
    private DocumentReference studentNumberRefId;
    private DocumentReference eventRefId;
    private DocumentReference locationRefId;
    @ServerTimestamp
    private Date timeIn;
    private Date timeOut;
    private String reason;
    private AttendanceStatus attendanceStatus;
    private DocumentReference updatedByUserRefId;
    @ServerTimestamp
    private Date updatedAt;
}
