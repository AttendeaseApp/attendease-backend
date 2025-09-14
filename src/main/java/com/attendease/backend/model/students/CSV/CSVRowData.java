package com.attendease.backend.model.students.CSV;

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
    private String courseRefId;
    private Date birthdate;
    private String address;
    private String contactNumber;
}
