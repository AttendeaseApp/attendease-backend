package com.attendease.backend.eventSessionManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventSessionCreateDTO {

    @NotBlank(message = "Event name is required")
    private String eventName;

    private String description;

    private String locationId;

    @NotNull(message = "Start date time is required")
    private String startDateTime;

    @NotNull(message = "End date time is required")
    private String endDateTime;
}
