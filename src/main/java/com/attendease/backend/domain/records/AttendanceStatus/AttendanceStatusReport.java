package com.attendease.backend.domain.records.AttendanceStatus;

import com.attendease.backend.domain.students.Students;
import lombok.Data;

import java.util.List;

@Data
public class AttendanceStatusReport {
    private List<Students> checkedInStudents;
    private List<Students> missingStudents;

    public AttendanceStatusReport(List<Students> checkedInStudents, List<Students> missingStudents) {
        this.checkedInStudents = checkedInStudents;
        this.missingStudents = missingStudents;
    }
}
