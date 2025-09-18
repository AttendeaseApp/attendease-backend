package com.attendease.backend.domain.events.EventCreation;

import com.attendease.backend.domain.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class EventCreationResponse {
    private String id;
    private String eventName;
    private String description;
    private String eventLocationRefId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Manila")
    private Date startDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Manila")
    private Date endDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Manila")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Manila")
    private Date updatedAt;

    private EventStatus eventStatus;
    private String createdByUserRefId;

    private boolean allStudents;
    private List<String> course;
    private List<String> sections;
}
