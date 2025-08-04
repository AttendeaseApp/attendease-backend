package com.attendease.attendease_backend.data.records;

import com.attendease.attendease_backend.enums.AttendanceStatus;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

@Data
public class Attendance {

    @DocumentId
    private String recordId;
    private DocumentReference studentRefId;
    private DocumentReference eventRefId;
    private DocumentReference locationRefId;
    private Date timeIn;
    private Date timeOut;
    private AttendanceStatus attendanceStatus;
    private DocumentReference updatedByUserRefId;
    @ServerTimestamp
    private Date updatedAt;
}
