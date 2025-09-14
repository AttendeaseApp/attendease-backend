package com.attendease.backend.model.events.Response;

import com.attendease.backend.model.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class EventCreationResponse {
    private String eventId;
    private String eventName;
    private String description;
    private String eventLocationRefId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDateTime;

    private EventStatus eventStatus;
    private String createdByUserRefId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
}
