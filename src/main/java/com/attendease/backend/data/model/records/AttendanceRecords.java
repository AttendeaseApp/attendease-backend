package com.attendease.backend.data.model.records;

import com.attendease.backend.data.model.enums.AttendanceStatus;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Data;

import java.util.Date;

@Data
public class AttendanceRecords {

    @DocumentId
    private String recordId;
    private DocumentReference studentNumberRefId;
    private DocumentReference eventRefId;
    private DocumentReference locationRefId;
    private Date timeIn;
    private Date timeOut;
    private AttendanceStatus attendanceStatus;
    private DocumentReference updatedByUserRefId;
    @ServerTimestamp
    private Date updatedAt;
}
