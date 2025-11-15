package com.attendease.backend.domain.events.Registration.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventStartStatusResponse {

    private String eventId;
    private boolean eventHasStarted;
    private boolean eventIsOngoing;
    private boolean eventHasEnded;
    private String statusMessage;
}
