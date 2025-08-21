package com.attendease.backend.userManagement.dto;

import com.attendease.backend.model.students.Students;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class StudentDTO {

    private String userRefId;
    private String facialRefID;
    private String studentNumber;
    private String section;
    private String yearLevel;
    private String courseRefId;
    private String clusterRefId;

    public StudentDTO() {}

    public StudentDTO(Students student) {
        this.userRefId = student.getUserRefId() != null ? student.getUserRefId().getPath() : null;
        this.facialRefID = student.getFacialRefID() != null ? student.getFacialRefID().getPath() : null;
        this.studentNumber = student.getStudentNumber();
        this.section = student.getSection();
        this.yearLevel = student.getYearLevel();
        this.courseRefId = student.getCourseRefId() != null ? student.getCourseRefId().getPath() : null;
        this.clusterRefId = student.getClusterRefId() != null ? student.getClusterRefId().getPath() : null;

        log.info("Mapped Student to StudentDTO: {}", this);
    }

    @Override
    public String toString() {
        return "StudentDTO{" +
                "userRefId='" + userRefId + '\'' +
                ", facialRefID='" + facialRefID + '\'' +
                ", studentNumber='" + studentNumber + '\'' +
                ", section='" + section + '\'' +
                ", yearLevel='" + yearLevel + '\'' +
                ", courseRefId='" + courseRefId + '\'' +
                ", clusterRefId='" + clusterRefId + '\'' +
                '}';
    }
}
