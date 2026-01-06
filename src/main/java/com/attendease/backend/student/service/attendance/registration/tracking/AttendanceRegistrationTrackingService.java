package com.attendease.backend.student.service.attendance.registration.tracking;

import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.location.Location;

/**
 * {@link AttendanceRegistrationTrackingService} is responsible for attendance verification during an event.
 * <p>
 * This service processes periodic geolocation "pings" sent by the student app to:
 * <ul>
 *     <li>Determine if the student is within the event's allowed geolocation boundary</li>
 *     <li>Append ping logs to the student's attendance record</li>
 *     <li>Ensure the event is ongoing before accepting location updates</li>
 * </ul>
 * It is typically used to monitor presence throughout the duration of an event.
 * </p>
 */
public interface AttendanceRegistrationTrackingService {

    /**
     * Processes and records a student's real-time location ping during an ongoing event.
     * <p>
     * This method:
     * <ul>
     *     <li>Validates the user and student identity</li>
     *     <li>Ensures the event is currently ongoing</li>
     *     <li>Checks if the student’s coordinates fall within the geofenced event location</li>
     *     <li>Appends the ping to the student's existing attendance record</li>
     * </ul>
     * </p>
     *
     * @param authenticatedUserId the ID of the authenticated user sending the ping
     * @param attendancePingLogs  the ping information containing GPS coordinates, event ID, and location ID
     * @return {@code true} if the student is inside the event boundary; {@code false} otherwise
     */
    boolean attendanceRegistrationTracker(String authenticatedUserId, AttendanceTrackingResponse attendancePingLogs);

	/**
	 * Retrieves the current venue location for an ongoing event.
	 * Useful for client apps to display which location is being monitored.
	 *
	 * @param eventId the event ID
	 * @return the venue location for monitoring
	 */
	Location getEventVenueForMonitoring(String eventId);
}
