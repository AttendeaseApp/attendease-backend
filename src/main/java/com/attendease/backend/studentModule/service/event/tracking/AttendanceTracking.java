package com.attendease.backend.studentModule.service.event.tracking;

import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.EventCheckIn.AttendancePingLogs;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.studentModule.service.utils.LocationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
     * Records a location ping for the authenticated student during an event.
     * Each ping logs the student's geolocation and updates their attendance record.
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

        AttendanceRecords attendanceRecord = attendanceRecordsRepository.findByStudentAndEventAndLocation(student, event, location)
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
