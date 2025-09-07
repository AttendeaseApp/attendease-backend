package com.attendease.backend.eventAttendanceMonitoringService.dto;

import com.attendease.backend.model.enums.EventStatus;
import com.attendease.backend.model.events.EventSessions;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class EventSessionsDto {
    private String eventId;
    private String eventName;
    private String eventLocationId;
    private String eventStatus;
    private String locationId;
    private Date startDate;
    private Date endDate;
    private Date createdAt;
    private Date updatedAt;

    public EventSessionsDto(String eventId, String eventName, String s, String description, String eligibleStudents, String academicTerm, Date startDateTime, Date endDateTime, EventStatus eventStatus, String s1, Date createdAt, Date updatedAt) {
    }

    public EventSessionsDto(String eventId, String eventName, String eventLocationId, String eventStatus, String locationId, Date startDate, Date endDate, Date createdAt, Date updatedAt) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventLocationId = eventLocationId;
        this.eventStatus = eventStatus;
        this.locationId = locationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EventSessionsDto mapToDto(EventSessions session) {
        return new EventSessionsDto(
                session.getEventId(),
                session.getEventName(),
                session.getEventLocationRefId() != null ? session.getEventLocationRefId().getId() : null,
                session.getEventStatus() != null ? session.getEventStatus().name() : null,
                session.getCreatedByUserRefId() != null ? session.getCreatedByUserRefId().getId() : null,
                session.getStartDateTime(),
                session.getEndDateTime(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
