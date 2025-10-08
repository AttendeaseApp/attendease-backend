package com.attendease.backend.automatedAttendanceTrackingService.service;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EligibleAttendees.EligibilityCriteria;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.EventCheckIn.EventCheckIn;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
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
    private final UserRepository userRepository;

    public EventCheckIn checkInStudent(String authenticatedUserId, EventCheckIn eventCheckIn) {
        Users user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentsRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
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
      
        if (!isStudentEligibleForEvent(event, student)) {
           throw new IllegalStateException("Student is not eligible to check in for this event.");
        }

        EventLocations location = eventLocationsRepository.findById(eventCheckIn.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));

        double studentLat = eventCheckIn.getLatitude();
        double studentLon = eventCheckIn.getLongitude();

        if (!isWithinLocationBoundary(location, studentLat, studentLon)) {
            throw new IllegalStateException("Student is outside the event location boundary");
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

    private boolean isStudentEligibleForEvent(EventSessions event, Students student) {
        EligibilityCriteria criteria = event.getEligibleStudents();
        if (criteria == null) return false;

        if (criteria.isAllStudents()) return true;

        String studentCourseId = student.getCourseId();
        String studentSectionId = student.getSectionId();

        boolean courseMatch = criteria.getCourse() != null && criteria.getCourse().contains(studentCourseId);
        boolean sectionMatch = criteria.getSections() != null && criteria.getSections().contains(studentSectionId);

        return courseMatch || sectionMatch;
    }

    private boolean isWithinLocationBoundary(EventLocations location, double latitude, double longitude) {
        GeoJsonPolygon polygon = location.getGeometry();
        if (polygon == null) {
            log.warn("No polygon geometry found for location: {}", location.getLocationId());
            return false;
        }

        List<GeoJsonLineString> lineStrings = polygon.getCoordinates();
        if (lineStrings.isEmpty()) {
            log.warn("No coordinates found in polygon for location: {}", location.getLocationId());
            return false;
        }

        GeoJsonLineString outerRing = lineStrings.getFirst();
        if (outerRing == null) {
            log.warn("No outer ring found in polygon for location: {}", location.getLocationId());
            return false;
        }

        List<Point> points = outerRing.getCoordinates();
        boolean isInside = isPointInPolygon(latitude, longitude, points);

        log.info("Student location check: [{}, {}] is {} the polygon boundary for location: {}",
                latitude, longitude, isInside ? "INSIDE" : "OUTSIDE", location.getLocationId());

        return isInside;
    }

    private boolean isPointInPolygon(double latitude, double longitude, List<Point> polygonPoints) {
        int n = polygonPoints.size();
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = polygonPoints.get(i);
            Point pj = polygonPoints.get(j);

            double xi = pi.getX(); //longitude
            double yi = pi.getY(); //latitude
            double xj = pj.getX(); //longitude
            double yj = pj.getY(); //latitude

            if (((yi > latitude) != (yj > latitude)) && (longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        return inside;
    }
}

