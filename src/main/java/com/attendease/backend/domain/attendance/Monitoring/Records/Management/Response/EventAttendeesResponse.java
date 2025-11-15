package com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response.AttendeesResponse;

@Data
@Builder
public class EventAttendeesResponse {
    private int totalAttendees;
    private List<AttendeesResponse> attendees;
}
