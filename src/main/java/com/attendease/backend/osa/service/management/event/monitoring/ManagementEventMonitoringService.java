package com.attendease.backend.osa.service.management.event.monitoring;

import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.event.Event;

import java.util.List;

/**
 * {@link ManagementEventMonitoringService} a service used for monitoring ongoing event attendees and event statuses.
 *
 * <p>Provides methods to retrieve ongoing events and fetch attendees</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-11
 */
public interface ManagementEventMonitoringService {

    /**
     * {@code getEventWithUpcomingRegistrationOngoingStatuses} is used to retrieve
     * all events with statuses UPCOMING, REGISTRATION, or ONGOING.
     *
     * @return a list of {@link Event} that are in any of the specified statuses
     */
    List<Event> getEventWithUpcomingRegistrationOngoingStatuses();

    /**
     * {@code getAttendeesByEventWithRegisteredAttendanceStatus} is used to retrieve
     * attendees for a specific event who currently have a REGISTERED attendance status.
     * This is used to monitor events and view participants who have registered.
     *
     * @param eventId the ID of the event to retrieve attendees for
     * @return an {@link EventAttendeesResponse} containing total registered attendees and their details
     */
    EventAttendeesResponse getAttendeesByEventWithRegisteredAttendanceStatus(String eventId);
}
