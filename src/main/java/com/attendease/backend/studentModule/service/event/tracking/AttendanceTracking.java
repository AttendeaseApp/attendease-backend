package com.attendease.backend.studentModule.service.event.tracking;

import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.EventRegistration.AttendancePingLogs;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.studentModule.service.utils.LocationValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AttendanceTracking service is responsible for attendance tracking during an event.
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
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceTracking {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final LocationRepository eventLocationsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;
    private final LocationValidator locationValidator;

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
     *
     * @throws IllegalStateException if:
     *         <ul>
     *             <li>The user or student profile does not exist</li>
     *             <li>The event or event location cannot be found</li>
     *             <li>The event is not currently ongoing</li>
     *             <li>The student does not have a prior registration record</li>
     *         </ul>
     */
    public boolean checkpointLocationPing(String authenticatedUserId, AttendancePingLogs attendancePingLogs) {
        Users user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentsRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        EventSessions event = eventSessionsRepository.findById(attendancePingLogs.getEventId()).orElseThrow(() -> new IllegalStateException("Event not found"));
        EventLocations location = eventLocationsRepository.findById(attendancePingLogs.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getStartDateTime()) || now.isAfter(event.getEndDateTime())) {
            throw new IllegalStateException("Event is not ongoing. Cannot record attendance ping.");
        }

        boolean isInside = locationValidator.isWithinLocationBoundary(location, attendancePingLogs.getLatitude(), attendancePingLogs.getLongitude());
        attendancePingLogs.setInside(isInside);
        attendancePingLogs.setTimestamp(System.currentTimeMillis());

        log.info("Attendance ping: Student={} Event={} Inside={}", student.getStudentNumber(), event.getEventId(), isInside);

        AttendanceRecords attendanceRecord = attendanceRecordsRepository
            .findByStudentAndEventAndLocation(student, event, location)
            .orElseThrow(() -> new IllegalStateException("Student must register before sending location pings"));

        if (attendanceRecord.getAttendancePingLogs() == null) {
            attendanceRecord.setAttendancePingLogs(new ArrayList<>());
        }
        attendanceRecord.getAttendancePingLogs().add(attendancePingLogs);
        attendanceRecord.setUpdatedByUserId(authenticatedUserId);
        attendanceRecord.setUpdatedAt(LocalDateTime.now());

        attendanceRecordsRepository.save(attendanceRecord);
        log.info("Attendance record updated successfully for student {} and event {}", student.getStudentNumber(), event.getEventId());
        return isInside;
    }
}
