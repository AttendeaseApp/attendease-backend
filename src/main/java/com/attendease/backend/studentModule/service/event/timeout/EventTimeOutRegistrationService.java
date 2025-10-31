package com.attendease.backend.studentModule.service.event.timeout;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.EventCheckIn.LocationPingRequest;
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

import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventTimeOutRegistrationService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final LocationRepository eventLocationsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;
    private final LocationValidator locationValidator;

    private final Date now = new Date();

//    public void eventTimeOutRegistration(String authenticatedUserId, LocationPingRequest request) {
//        Users user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
//        Students student = studentsRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
//        EventSessions event = eventSessionsRepository.findById(request.getEventId()).orElseThrow(() -> new IllegalStateException("Event not found"));
//        EventLocations location = eventLocationsRepository.findById(request.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));
//
//        Date start = event.getStartDateTime();
//        Date end = event.getEndDateTime();
//        Date allowedCheckoutEnd = Date.from(end.toInstant().plus(15, ChronoUnit.MINUTES));
//
//        if (now.before(start)) {
//            throw new IllegalStateException("You cannot check out before the event starts.");
//        }
//
//        if (now.after(allowedCheckoutEnd)) {
//            throw new IllegalStateException("Checkout window has expired. Event already finalized or past allowed time.");
//        }
//
//        AttendanceRecords record = attendanceRecordsRepository.findByStudentAndEvent(student, event);
//        if (record == null) {
//            throw new IllegalStateException("No check-in record found for this student and event.");
//        }
//
//        if (record.getAttendanceStatus() == AttendanceStatus.ABSENT) {
//            throw new IllegalStateException("You were marked absent for this event. Time out registration already unavailable :(");
//        }
//
//        boolean isInside = locationValidator.isWithinLocationBoundary(location, request.getLatitude(), request.getLongitude());
//        if (!isInside) {
//            throw new IllegalStateException("Cannot check out while outside the event area.");
//        }
//
//        AttendanceRecords finalRecord = AttendanceRecords.builder()
//                .student(student)
//                .event(event)
//                .location(location)
//                .timeIn(null)
//                .attendanceStatus(AttendanceStatus.PRESENT)
//                .build();
//
//        attendanceRecordsRepository.save(finalRecord);
//        log.info("Student {} successfully checked out from event {}", student.getStudentNumber(), event.getEventId());
//    }
}
