package com.attendease.backend.student.service.event.registration.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.biometrics.Verification.Response.BiometricsVerificationResponse;
import com.attendease.backend.domain.biometrics.Verification.Response.EventRegistrationBiometricsVerificationResponse;
import com.attendease.backend.domain.cluster.Cluster;
import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.event.registration.EventRegistrationRequest;
import com.attendease.backend.domain.location.Location;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.student.service.event.registration.EventRegistrationService;
import com.attendease.backend.student.service.utils.BiometricsVerificationClient;
import com.attendease.backend.student.service.utils.LocationValidator;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventRepository eventRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final BiometricsVerificationClient biometricsVerificationService;
    private final BiometricsRepository biometricsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;
    private final LocationValidator locationValidator;

    @Override
    public EventRegistrationRequest eventRegistration(String authenticatedUserId, EventRegistrationRequest registrationRequest) {

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentsRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        Event event = eventRepository.findById(registrationRequest.getEventId())
                .orElseThrow(() -> new IllegalStateException("Event not found"));

        LocalDateTime now = LocalDateTime.now();

        validateEventStatus(event);

        if (!isStudentEligibleForEvent(event, student)) {
            throw new IllegalStateException("Student is not eligible to check in for this event.");
        }

        Location registrationLocation = event.getRegistrationLocation();
        Location venueLocation = event.getVenueLocation();

        if (registrationLocation == null) {
            log.error("Event {} has no registration location configured", event.getEventId());
            throw new IllegalStateException("Event registration location is not configured");
        }

        if (venueLocation == null) {
            log.error("Event {} has no venue location configured", event.getEventId());
            throw new IllegalStateException("Event venue location is not configured");
        }

        boolean withinRegistrationLocation = locationValidator.isWithinLocationBoundary(
                registrationLocation,
                registrationRequest.getLatitude(),
                registrationRequest.getLongitude());

        boolean withinVenueLocation = locationValidator.isWithinLocationBoundary(
                venueLocation,
                registrationRequest.getLatitude(),
                registrationRequest.getLongitude());

        if (!withinRegistrationLocation && !withinVenueLocation) {
            log.warn("Student {} attempted registration outside both registration and venue location boundaries for event {}",
                    student.getStudentNumber(), event.getEventId());
            throw new IllegalStateException(
                    String.format("You must be at either the registration location (%s) or venue location (%s) to check in for this event.",
                            registrationLocation.getLocationName(),
                            venueLocation.getLocationName()));
        }

        Optional<AttendanceRecords> existingRecord = attendanceRecordsRepository.findByStudentAndEvent(student, event);
        boolean strictValidation = event.getStrictLocationValidation() != null && event.getStrictLocationValidation();

        if (existingRecord.isPresent()) {
            AttendanceRecords record = existingRecord.get();

            if (strictValidation &&
                    record.getAttendanceStatus() == AttendanceStatus.PARTIALLY_REGISTERED &&
                    withinVenueLocation) {

                upgradeToFullRegistration(record, venueLocation, now, event);

                log.info("Student {} upgraded from PARTIALLY_REGISTERED to REGISTERED at venue for event {}",
                        student.getStudentNumber(), event.getEventId());

                return registrationRequest;
            }

            throw new IllegalStateException(
                    String.format("You are already registered for this event (Status: %s, Registered at: %s)",
                            record.getAttendanceStatus(), record.getTimeIn()));
        }

        if (event.getFacialVerificationEnabled() && !event.getAttendanceLocationMonitoringEnabled()) {
            if (registrationRequest.getFaceImageBase64() == null || registrationRequest.getFaceImageBase64().isEmpty()) {
                throw new IllegalStateException("Face image is required for check-in when facial verification is enabled");
            }
            verifyStudentFace(student.getStudentNumber(), registrationRequest.getFaceImageBase64());
        }

        AttendanceStatus initialStatus = determineInitialStatus(
                strictValidation,
                withinVenueLocation,
                now,
                event.getStartingDateTime());

        Location checkedInLocation = withinVenueLocation ? venueLocation : registrationLocation;

        AttendanceRecords record = AttendanceRecords.builder()
                .student(student)
                .event(event)
                .location(checkedInLocation)
                .eventLocationId(checkedInLocation.getLocationId())
                .academicYear(event.getAcademicYear())
                .academicYearId(event.getAcademicYearId())
                .academicYearName(event.getAcademicYearName())
                .semester(event.getSemester())
                .semesterName(event.getSemesterName())
                .timeIn(now)
                .attendanceStatus(initialStatus)
                .reason(getInitialReason(initialStatus))
                .build();

        attendanceRecordsRepository.save(record);
        return registrationRequest;
    }

    /**
     * PRIVATE HELPERS
     */

    private AttendanceStatus determineInitialStatus(boolean strictValidation, boolean withinVenueLocation, LocalDateTime now, LocalDateTime eventStart) {
        boolean isLate = now.isAfter(eventStart);
        if (!strictValidation) {
            return isLate ? AttendanceStatus.LATE : AttendanceStatus.REGISTERED;
        }

        if (withinVenueLocation) {
            return isLate ? AttendanceStatus.LATE : AttendanceStatus.REGISTERED;
        } else {
            return AttendanceStatus.PARTIALLY_REGISTERED;
        }
    }

    private String getInitialReason(AttendanceStatus status) {
        if (status == AttendanceStatus.LATE) {
            return "Late registration";
        }
        if (status == AttendanceStatus.PARTIALLY_REGISTERED) {
            return "Checked in at registration area. Please proceed to the venue to complete registration.";
        }

        return null;
    }

    private void upgradeToFullRegistration(AttendanceRecords record,Location venueLocation,LocalDateTime now,Event event) {
        boolean isLate = now.isAfter(event.getStartingDateTime());
        record.setAttendanceStatus(isLate ? AttendanceStatus.LATE : AttendanceStatus.REGISTERED);
        record.setLocation(venueLocation);
        record.setEventLocationId(venueLocation.getLocationId());
        record.setReason(isLate ? "Late arrival at venue" : "Completed registration at venue");
        record.setTimeIn(now);
        attendanceRecordsRepository.save(record);
    }

    private void validateEventStatus(Event event) {
        EventStatus status = event.getEventStatus();

        if (status == EventStatus.UPCOMING) {
            throw new IllegalStateException(
                    String.format("Registration not yet open. It starts at %s.",
                            event.getRegistrationDateTime()));
        }

        if (status == EventStatus.CONCLUDED) {
            throw new IllegalStateException(
                    "This event has already ended and is at the stage of finalizing records. You can no longer register.");
        }

        if (status == EventStatus.FINALIZED) {
            throw new IllegalStateException("The event has already ended and records were finalized.");
        }
    }

    private void verifyStudentFace(String studentNumber, String faceImageBase64) {

        try {
            BiometricData biometricData = biometricsRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new IllegalStateException(
                            "No biometric data found for student. Please register your face first."));

            if (biometricData.getFacialEncoding() == null || biometricData.getFacialEncoding().isEmpty()) {
                throw new IllegalStateException("Student's facial encoding is not registered");
            }

            EventRegistrationBiometricsVerificationResponse encodingResponse =
                    biometricsVerificationService.extractFaceEncoding(faceImageBase64);

            if (!encodingResponse.getSuccess() || encodingResponse.getFacialEncoding() == null) {
                throw new IllegalStateException("Failed to detect face in uploaded image");
            }

            BiometricsVerificationResponse verificationResponse =
                    biometricsVerificationService.verifyFace(
                            encodingResponse.getFacialEncoding(),
                            biometricData.getFacialEncoding());

            if (!verificationResponse.getSuccess() || !verificationResponse.getIs_face_matched()) {
                throw new IllegalStateException("Facial verification failed. Please try again with better lighting.");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Facial verification error for student {}: {}", studentNumber, e.getMessage(), e);
            throw new IllegalStateException("Facial verification error: " + e.getMessage());
        }
    }

    private boolean isStudentEligibleForEvent(Event event, Students student) {

        EventEligibility criteria = event.getEligibleStudents();

        if (criteria == null || criteria.isAllStudents()) {
            return true;
        }
        if (student.getSection() == null) {
            return false;
        }

        Course course = student.getSection().getCourse();
        Cluster cluster = (course != null) ? course.getCluster() : null;

        if (criteria.getSections() != null
                && criteria.getSections().contains(student.getSection().getId())) {
            return true;
        }

        if (criteria.getCourses() != null
                && course != null
                && criteria.getCourses().contains(course.getId())) {
            return true;
        }

        return criteria.getClusters() != null
                && cluster != null
                && criteria.getClusters().contains(cluster.getClusterId());
    }
}