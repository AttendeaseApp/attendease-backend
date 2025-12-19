package com.attendease.backend.student.service.attendance.registration.tracking.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.student.service.attendance.registration.tracking.AttendanceRegistrationTrackingService;
import com.attendease.backend.student.service.utils.LocationValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceRegistrationTrackingServiceImpl implements AttendanceRegistrationTrackingService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final LocationRepository eventLocationsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;
    private final LocationValidator locationValidator;

    @Override
    public boolean attendanceRegistrationTracker(String authenticatedUserId, AttendanceTrackingResponse attendancePingLogs) {
        User user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
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
        return isInside;
    }
}
