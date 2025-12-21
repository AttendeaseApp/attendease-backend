package com.attendease.backend.domain.event.state.checking;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO representing the current state of an event for a student.
 */
@Data
@AllArgsConstructor
public final class EventStateCheckingResponse {

    private String eventId;
    private boolean eventHasStarted;
    private boolean eventIsOngoing;
    private boolean eventHasEnded;
    private String statusMessage;
}
