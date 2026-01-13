package com.attendease.backend.student.service.location.verification.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.location.Location;
import com.attendease.backend.domain.location.tracking.LocationTrackingRequest;
import com.attendease.backend.domain.location.tracking.LocationTrackingResponse;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.location.LocationRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.student.service.location.verification.LocationVerificationService;
import com.attendease.backend.student.service.utils.LocationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for verifying student location against event venues.
 * <p>
 * This service handles real-time location verification for ongoing events,
 * checking if students are within the venue location boundaries during
 * the event session (not during registration).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationVerificationServiceImpl implements LocationVerificationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final StudentRepository studentRepository;
    private final LocationValidator locationValidator;

    @Override
    public LocationTrackingResponse verifyEventVenueLocation(String eventId, double latitude, double longitude) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Event not found"));

        Location venueLocation = event.getVenueLocation();
        if (venueLocation == null) {
            log.error("Event {} has no venue location configured", eventId);
            throw new IllegalStateException("Event venue location is not configured");
        }

        boolean isInside = locationValidator.isWithinLocationBoundary(
                venueLocation,
                latitude,
                longitude);

        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setInside(isInside);
        response.setMessage(isInside
                ? String.format("You are inside the event venue (%s).", venueLocation.getLocationName())
                : String.format("Warning: You are outside the event venue (%s). Please return to the venue area.",
                venueLocation.getLocationName()));

        log.info("Venue location tracking for event {}: Student is {} venue {}",
                eventId,
                isInside ? "INSIDE" : "OUTSIDE",
                venueLocation.getLocationName());

        return response;
    }

    @Override
    public LocationTrackingResponse verifyEventRegistrationLocation(String eventId, double latitude, double longitude) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Event not found"));

        Location registrationLocation = event.getRegistrationLocation();
        if (registrationLocation == null) {
            log.error("Event {} has no registration location configured", eventId);
            throw new IllegalStateException("Event registration location is not configured");
        }

        boolean isInside = locationValidator.isWithinLocationBoundary(
                registrationLocation,
                latitude,
                longitude);

        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setInside(isInside);
        response.setMessage(isInside
                ? String.format("You are at the registration location (%s). You may proceed with check-in.",
                registrationLocation.getLocationName())
                : String.format("You must be at the registration location (%s) to check in for this event.",
                registrationLocation.getLocationName()));

        log.info("Registration location tracking for event {}: Student is {} registration area {}",
                eventId,
                isInside ? "INSIDE" : "OUTSIDE",
                registrationLocation.getLocationName());

        return response;
    }

    @Override
    public LocationTrackingResponse verifyEventVenueLocationWithAutoUpgrade(String authenticatedUserId,String eventId,double latitude,double longitude) {

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalStateException("Event not found"));
        Location venueLocation = event.getVenueLocation();
        if (venueLocation == null) {
            throw new IllegalStateException("Event venue location is not configured");
        }

        boolean isInside = locationValidator.isWithinLocationBoundary(venueLocation,latitude,longitude);
        LocationTrackingResponse response = new LocationTrackingResponse();
        response.setInside(isInside);
        boolean strictValidation = event.getStrictLocationValidation() != null && event.getStrictLocationValidation();

        if (isInside && strictValidation) {
            boolean upgraded = tryAutoUpgradeToVenue(authenticatedUserId,event,venueLocation);
            if (upgraded) {
                response.setMessage(String.format(
                        "Welcome to %s! Your registration has been completed automatically.",
                        venueLocation.getLocationName()));
                response.setAutoUpgraded(true);
            } else {
                response.setMessage(String.format("You are inside the event venue (%s).",venueLocation.getLocationName()));
                response.setAutoUpgraded(false);
            }
        } else if (isInside) {
            response.setMessage(String.format("You are inside the event venue (%s).", venueLocation.getLocationName()));
            response.setAutoUpgraded(false);
        } else {
            response.setMessage(String.format("Warning: You are outside the event venue (%s). Please return to the venue area.",venueLocation.getLocationName()));
            response.setAutoUpgraded(false);
        }
        return response;
    }

    private boolean tryAutoUpgradeToVenue(String authenticatedUserId, Event event, Location venueLocation) {
        try {
            User user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("User not found"));
            Students student = studentRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Student not found"));
            Optional<AttendanceRecords> recordOpt = attendanceRecordsRepository.findByStudentAndEvent(student, event);

            if (recordOpt.isEmpty()) {
                return false;
            }

            AttendanceRecords record = recordOpt.get();

            if (record.getAttendanceStatus() != AttendanceStatus.PARTIALLY_REGISTERED) {
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            boolean isLate = now.isAfter(event.getStartingDateTime());

            record.setAttendanceStatus(isLate ? AttendanceStatus.LATE : AttendanceStatus.REGISTERED);
            record.setLocation(venueLocation);
            record.setEventLocationId(venueLocation.getLocationId());
            record.setReason(isLate ? "Late arrival at venue (auto-upgraded)" : "Completed registration at venue (auto-upgraded)");
            record.setTimeIn(now);

            attendanceRecordsRepository.save(record);

            log.info("Successfully auto-upgraded student {} from PARTIALLY_REGISTERED to {} for event {}",
                    student.getStudentNumber(),
                    record.getAttendanceStatus(),
                    event.getEventId());

            return true;

        } catch (Exception e) {
            log.error("Failed to auto-upgrade student: {}", e.getMessage(), e);
            return false;
        }
    }
}