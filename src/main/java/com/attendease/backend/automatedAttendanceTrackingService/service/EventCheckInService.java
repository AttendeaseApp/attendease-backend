package com.attendease.backend.automatedAttendanceTrackingService.service;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.locations.Geofencing.GeofenceData;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.EventCheckIn.EventCheckIn;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventCheckInService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final LocationRepository eventLocationsRepository;
    private final StudentRepository studentsRepository;

    public EventCheckIn checkInStudent(String studentNumber, EventCheckIn eventCheckIn) {
        Students student = studentsRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalStateException("Student not found"));
        EventSessions event = eventSessionsRepository.findById(eventCheckIn.getEventId()).orElseThrow(() -> new IllegalStateException("Event not found"));

        Date now = new Date();
        Date startTime = event.getStartDateTime();
        Date endTime = event.getEndDateTime();
        Date registrationStartTime = new Date(startTime.getTime() - 30 * 60 * 1000);

        if (now.before(registrationStartTime)) {
            throw new IllegalStateException(String.format("Cannot check in yet. Registration opens at %s. Event starts at %s.", registrationStartTime, startTime));
        }

        if (now.after(endTime)) {
            throw new IllegalStateException("Event has already ended. You can no longer check in.");
        }

        EventLocations location = eventLocationsRepository.findById(eventCheckIn.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));

        //check geofence
        GeofenceData geofence = location.getGeofenceParameters();
        if (geofence == null) {
            throw new IllegalStateException("Geofence parameters not found for location");
        }

        double studentLat = eventCheckIn.getLatitude();
        double studentLon = eventCheckIn.getLongitude();

        if (!isWithinGeofence(
                studentLat,
                studentLon,
                geofence.getCenterLatitude(),
                geofence.getCenterLongitude(),
                geofence.getRadiusMeters())) {
            throw new IllegalStateException("Student is outside the event geofence");
        }

        isAlreadyCheckedIn(student, event, location);

        AttendanceRecords record = AttendanceRecords.builder()
                .student(student)
                .event(event)
                .location(location)
                .timeIn(null)
                .attendanceStatus(AttendanceStatus.CHECKED_IN)
                .build();

        attendanceRecordsRepository.save(record);

        return eventCheckIn;
    }

    private void isAlreadyCheckedIn(Students student, EventSessions event, EventLocations location) {
        List<AttendanceRecords> existingRecords = attendanceRecordsRepository.findByStudentAndEventAndLocationAndAttendanceStatus(student, event, location, AttendanceStatus.CHECKED_IN);

        if (!existingRecords.isEmpty()) {
            throw new IllegalStateException("Student is already checked in for this event/location.");
        }
    }

    private boolean isWithinGeofence(double lat1, double lon1, double lat2, double lon2, double radiusMeters) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        log.info("Calculated distance: {} meters (allowed radius: {}, student: [{}, {}], geofence center: [{}, {}])",
                distance, radiusMeters, lat1, lon1, lat2, lon2);
        return distance <= radiusMeters;
    }
}

