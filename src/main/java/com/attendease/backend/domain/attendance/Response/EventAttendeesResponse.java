package com.attendease.backend.domain.attendance.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventAttendeesResponse {
    private int totalAttendees;
    private List<AttendeesResponse> attendees;
}
