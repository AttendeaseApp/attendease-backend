package com.attendease.backend.studentModule.service.event.tracking;

import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.locations.Request.CheckCurrentLocationRequest;
import com.attendease.backend.domain.locations.Response.CheckCurrentLocationResponse;
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
public class CheckCurrentLocation {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final LocationRepository eventLocationsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;
    private final LocationValidator locationValidator;

    public CheckCurrentLocationResponse checkMyCurrentLocationPosition(String authenticatedUserId, CheckCurrentLocationRequest request) {
        Users user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentsRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        EventLocations location = eventLocationsRepository.findById(request.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));

        boolean isInside = locationValidator.isWithinLocationBoundary(location, request.getLatitude(), request.getLongitude());
        log.info("Attendance ping: Student={} Inside={}", student.getStudentNumber(), isInside);

        CheckCurrentLocationResponse response = new CheckCurrentLocationResponse();
        response.setInside(isInside);
        response.setMessage(isInside ? "You are inside the location" : "You are outside the location");

        return response;
    }

}
