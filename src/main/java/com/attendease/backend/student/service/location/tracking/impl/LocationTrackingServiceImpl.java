package com.attendease.backend.student.service.location.tracking.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.location.Location;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.student.service.location.tracking.LocationTrackingService;
import com.attendease.backend.student.service.utils.LocationValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for tracking student attendance through periodic location pings.
 * <p>
 * This service monitors students during ongoing events to ensure they remain
 * within the venue location boundaries. It works in conjunction with the
 * event's {@code attendanceLocationMonitoringEnabled} flag.
 * </p>
 * <p>
 * <b>Location Flow:</b>
 * <ol>
 *   <li>Student checks in at the <b>registration location</b></li>
 *   <li>During the event, student sends location pings</li>
 *   <li>Pings are validated against the <b>venue location</b> (not registration location)</li>
 *   <li>Attendance status may be updated based on venue boundary compliance</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationTrackingServiceImpl implements LocationTrackingService {

    private final EventRepository eventRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;
    private final LocationValidator locationValidator;

    @Override
    public boolean venueLocationMonitoring(String authenticatedUserId, AttendanceTrackingResponse attendancePingLogs) {

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentsRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        Event event = eventRepository.findById(attendancePingLogs.getEventId())
                .orElseThrow(() -> new IllegalStateException("Event not found"));

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(event.getStartingDateTime()) || now.isAfter(event.getEndingDateTime())) {
            throw new IllegalStateException("Event is not ongoing. Cannot record attendance ping.");
        }
        if (!event.getAttendanceLocationMonitoringEnabled()) {
            log.warn("Location monitoring is not enabled for event {}. Ping rejected.", event.getEventId());
            throw new IllegalStateException("Location monitoring is not enabled for this event.");
        }
        Location venueLocation = event.getVenueLocation();
        if (venueLocation == null) {
            log.error("Event {} has no venue location configured for monitoring", event.getEventId());
            throw new IllegalStateException("Event venue location is not configured for monitoring");
        }
        if (!venueLocation.getLocationId().equals(attendancePingLogs.getLocationId())) {
            log.error("Location ID mismatch. Expected venue location {}, but received {}",
                    venueLocation.getLocationId(), attendancePingLogs.getLocationId());
            throw new IllegalStateException(
                    String.format("Invalid location ID. You should be tracking at venue location: %s",
                            venueLocation.getLocationName()));
        }

        boolean isInside = locationValidator.isWithinLocationBoundary(
                venueLocation,
                attendancePingLogs.getLatitude(),
                attendancePingLogs.getLongitude());

        attendancePingLogs.setInside(isInside);
        attendancePingLogs.setTimestamp(System.currentTimeMillis());

        AttendanceRecords attendanceRecord = attendanceRecordsRepository
                .findByStudentAndEvent(student, event)
                .orElseThrow(() -> new IllegalStateException(
                        "No attendance record found. Student must register at the registration location before sending location pings."));

        if (attendanceRecord.getAttendanceStatus() != AttendanceStatus.REGISTERED
                && attendanceRecord.getAttendanceStatus() != AttendanceStatus.LATE
                && attendanceRecord.getAttendanceStatus() != AttendanceStatus.PRESENT) {
            throw new IllegalStateException(
                    "Student must be in REGISTERED, LATE, or PRESENT status to send location pings.");
        }
        if (attendanceRecord.getAttendancePingLogs() == null) {
            attendanceRecord.setAttendancePingLogs(new ArrayList<>());
        }

        attendanceRecord.getAttendancePingLogs().add(attendancePingLogs);

        attendanceRecord.setUpdatedByUserId(authenticatedUserId);
        attendanceRecord.setUpdatedAt(LocalDateTime.now());

        if (!isInside) {
            log.warn("Student {} is outside venue {} during event {}",
                    student.getStudentNumber(),
                    venueLocation.getLocationName(),
                    event.getEventId());

            long consecutiveOutsidePings = attendanceRecord.getAttendancePingLogs().stream()
                    .skip(Math.max(0, attendanceRecord.getAttendancePingLogs().size() - 3))
                    .filter(ping -> !ping.isInside())
                    .count();

            if (consecutiveOutsidePings >= 3) {
                String existingReason = attendanceRecord.getReason();
                String newReason = "Student detected outside venue boundaries during event";

                if (existingReason != null && !existingReason.isEmpty()) {
                    attendanceRecord.setReason(existingReason + "; " + newReason);
                } else {
                    attendanceRecord.setReason(newReason);
                }

                log.warn("Student {} has {} consecutive pings outside venue - flagged for review",
                        student.getStudentNumber(),
                        consecutiveOutsidePings);
            }
        } else {
            log.info("Student {} location verified inside venue {} for event {}",
                    student.getStudentNumber(),
                    venueLocation.getLocationName(),
                    event.getEventId());
        }

        attendanceRecordsRepository.save(attendanceRecord);
        return isInside;
    }

    @Override
    public Location getEventVenueForMonitoring(String eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Event not found"));

        if (!event.getAttendanceLocationMonitoringEnabled()) {
            throw new IllegalStateException("Location monitoring is not enabled for this event");
        }
        Location venueLocation = event.getVenueLocation();
        if (venueLocation == null) {
            throw new IllegalStateException("Event venue location is not configured");
        }
        return venueLocation;
    }
}