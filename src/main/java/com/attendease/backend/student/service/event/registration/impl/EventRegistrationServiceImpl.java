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
import com.attendease.backend.client.biometrics.verification.BiometricsVerificationClient;
import com.attendease.backend.student.service.utils.LocationValidator;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public EventRegistrationRequest eventRegistration(String authenticatedUserId, EventRegistrationRequest registrationRequest, MultipartFile faceImage) {

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

        boolean strictValidation = event.getStrictLocationValidation() != null && event.getStrictLocationValidation();

        if (strictValidation) {
            Optional<AttendanceRecords> existingRecord = attendanceRecordsRepository.findByStudentAndEvent(student, event);
            if (existingRecord.isEmpty()) {
                if (!withinRegistrationLocation) {
                    log.warn("Student {} attempted strict validation registration outside registration location for event {}",
                            student.getStudentNumber(), event.getEventId());
                    throw new IllegalStateException(
                            String.format("For strict validation events, you must first register at the registration location (%s). You are currently %s.",
                                    registrationLocation.getLocationName(),
                                    withinVenueLocation ? "at the venue location" : "outside both locations"));
                }
            } else {
                AttendanceRecords record = existingRecord.get();
                if (record.getAttendanceStatus() == AttendanceStatus.PARTIALLY_REGISTERED && withinVenueLocation) {
                    upgradeToFullRegistration(record, venueLocation, now, event);
                    log.info("Student {} upgraded from PARTIALLY_REGISTERED to REGISTERED at venue for event {}",
                            student.getStudentNumber(), event.getEventId());
                    return registrationRequest;
                }

                throw new IllegalStateException(
                        String.format("You are already registered for this event (Status: %s, Registered at: %s)",
                                record.getAttendanceStatus(), record.getTimeIn()));
            }
        } else {
            if (!withinRegistrationLocation && !withinVenueLocation) {
                log.warn("Student {} attempted registration outside both registration and venue location boundaries for event {}",
                        student.getStudentNumber(), event.getEventId());
                throw new IllegalStateException(
                        String.format("You must be at either the registration location (%s) or venue location (%s) to check in for this event.",
                                registrationLocation.getLocationName(),
                                venueLocation.getLocationName()));
            }
            Optional<AttendanceRecords> existingRecord = attendanceRecordsRepository.findByStudentAndEvent(student, event);
            if (existingRecord.isPresent()) {
                AttendanceRecords record = existingRecord.get();
                throw new IllegalStateException(
                        String.format("You are already registered for this event (Status: %s, Registered at: %s)",
                                record.getAttendanceStatus(), record.getTimeIn()));
            }
        }

        if (event.getFacialVerificationEnabled() && !event.getAttendanceLocationMonitoringEnabled()) {
            if (faceImage == null || faceImage.isEmpty()) {
                throw new IllegalStateException("Face image is required for check-in when facial verification is enabled");
            }
            verifyStudentFace(student.getStudentNumber(), faceImage);
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
        log.info("Student {} registered for event {} with status {} at location {}", student.getStudentNumber(), event.getEventId(), initialStatus, checkedInLocation.getLocationName());
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

    private void upgradeToFullRegistration(AttendanceRecords record, Location venueLocation, LocalDateTime now, Event event) {
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

    private void verifyStudentFace(String studentNumber, MultipartFile faceImage) {
        try {
            BiometricData biometricData = biometricsRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new IllegalStateException(
                            "No biometric data found for student. Please register your face first."));

            if (biometricData.getFacialEncoding() == null || biometricData.getFacialEncoding().isEmpty()) {
                throw new IllegalStateException("Student's facial encoding is not registered");
            }

            log.info("Extracting facial encoding from uploaded image for student: {}", studentNumber);
            EventRegistrationBiometricsVerificationResponse encodingResponse = biometricsVerificationService.extractFaceEncoding(faceImage);

            if (!encodingResponse.getSuccess() || encodingResponse.getFacialEncoding() == null) {
                throw new IllegalStateException("Failed to detect face in uploaded image");
            }

            Double quality = encodingResponse.getQuality();
            if (quality != null) {
                log.info("Face detection quality score for student {}: {}", studentNumber, quality);
                if (quality < 50) {
                    log.warn("Low quality face detection ({}) for student {}", quality, studentNumber);
                }
            }

            log.info("Comparing facial encodings for student: {}", studentNumber);
            BiometricsVerificationResponse verificationResponse =
                    biometricsVerificationService.verifyFace(
                            encodingResponse.getFacialEncoding(),
                            biometricData.getFacialEncoding());

            if (!verificationResponse.getIs_face_matched()) {
                log.warn("Face verification failed for student {}. Distance: {}, Confidence: {}",
                        studentNumber,
                        verificationResponse.getFace_distance(),
                        verificationResponse.getConfidence());
                throw new IllegalStateException(
                        String.format("Facial verification failed (confidence: %.2f%%). Please try again with better lighting.",
                                (verificationResponse.getConfidence() != null ? verificationResponse.getConfidence() * 100 : 0)));
            }

            log.info("Face verification successful for student {}. Confidence: {}", studentNumber, verificationResponse.getConfidence());

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
        Integer studentYearLevel = student.getSection().getYearLevel();

        if (criteria.getSelectedSections() != null
                && !criteria.getSelectedSections().isEmpty()
                && criteria.getSelectedSections().contains(student.getSection().getId())) {
            return matchesYearLevelIfSpecified(criteria, studentYearLevel);
        }

        if (criteria.getSelectedCourses() != null
                && !criteria.getSelectedCourses().isEmpty()
                && course != null
                && criteria.getSelectedCourses().contains(course.getId())) {
            return matchesYearLevelIfSpecified(criteria, studentYearLevel);
        }

        if (criteria.getSelectedClusters() != null
                && !criteria.getSelectedClusters().isEmpty()
                && cluster != null
                && criteria.getSelectedClusters().contains(cluster.getClusterId())) {
            return matchesYearLevelIfSpecified(criteria, studentYearLevel);
        }

	    return (criteria.getSelectedSections() == null || criteria.getSelectedSections().isEmpty())
			    && (criteria.getSelectedCourses() == null || criteria.getSelectedCourses().isEmpty())
			    && (criteria.getSelectedClusters() == null || criteria.getSelectedClusters().isEmpty())
			    && criteria.getTargetYearLevels() != null
			    && !criteria.getTargetYearLevels().isEmpty()
			    && studentYearLevel != null
			    && criteria.getTargetYearLevels().contains(studentYearLevel);
    }

    private boolean matchesYearLevelIfSpecified(EventEligibility criteria, Integer studentYearLevel) {
        if (criteria.getTargetYearLevels() == null || criteria.getTargetYearLevels().isEmpty()) {
            return true;
        }
        return studentYearLevel != null && criteria.getTargetYearLevels().contains(studentYearLevel);
    }
}