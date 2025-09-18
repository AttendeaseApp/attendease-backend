package com.attendease.backend.domain.students.CSV;

import lombok.Data;

import java.util.Date;

@Data
public class CSVRowData {
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private String studentNumber;
    private String section;
    private String yearLevel;
    private String course;
    private Date birthdate;
    private String contactNumber;
}
