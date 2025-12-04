package com.attendease.backend.domain.users.CSV;

import lombok.Data;

@Data
public class CSVRowData {

    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private String studentNumber;
    private String sectionName;
    private String contactNumber;
}
