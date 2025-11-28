package com.attendease.backend.studentModule.service.event.registration;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.biometrics.Verification.Response.BiometricsVerificationResponse;
import com.attendease.backend.domain.biometrics.Verification.Response.EventRegistrationBiometricsVerificationResponse;
import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EligibleAttendees.EligibilityCriteria;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.events.Registration.Request.EventRegistrationRequest;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.studentModule.service.utils.BiometricsVerificationClient;
import com.attendease.backend.studentModule.service.utils.LocationValidator;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * EventRegistrationService is responsible for handling student event registrations.
 * <p>
 * This includes:
 * <ul>
 *     <li>Validating event registration time windows</li>
 *     <li>Confirming physical location proximity</li>
 *     <li>Performing biometric (facial) verification</li>
 *     <li>Ensuring the student is not already registered</li>
 *     <li>Creating attendance records upon successful registration</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventRegistrationService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final LocationRepository eventLocationsRepository;
    private final BiometricsVerificationClient biometricsVerificationService;
    private final BiometricsRepository biometricsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;
    private final LocationValidator locationValidator;

    /**
     * Registers a student for an event after validating:
     * <ul>
     *     <li>Event availability</li>
     *     <li>Location boundaries</li>
     *     <li>Duplicate registration prevention</li>
     *     <li>Biometric facial verification</li>
     * </ul>
     *
     * @param authenticatedUserId the ID of the currently authenticated user
     * @param registrationRequest the registration request containing event, location, and biometric data
     * @return the original registration request if successful
     *
     * @throws IllegalStateException if the user, student, event, location, or biometrics are invalid,
     *                               or if registration conditions are not met
     */
    public EventRegistrationRequest eventRegistration(String authenticatedUserId, EventRegistrationRequest registrationRequest) {
        Users user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentsRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        EventSessions event = eventSessionsRepository.findById(registrationRequest.getEventId()).orElseThrow(() -> new IllegalStateException("Event not found"));

        LocalDateTime now = LocalDateTime.now();
        validateEventStatus(event, now);

        if (!isStudentEligibleForEvent(event, student)) {
            throw new IllegalStateException("Student is not eligible to check in for this event.");
        }

        EventLocations location = eventLocationsRepository.findById(registrationRequest.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));

        if (!locationValidator.isWithinLocationBoundary(location, registrationRequest.getLatitude(), registrationRequest.getLongitude())) {
            throw new IllegalStateException("Student is outside the event location boundary");
        }

        isAlreadyRegistered(student, event, location);

        if (registrationRequest.getFaceImageBase64() == null) {
            throw new IllegalStateException("Face image is required for check-in");
        }

        verifyStudentFace(student.getStudentNumber(), registrationRequest.getFaceImageBase64());

        AttendanceStatus initialStatus = now.isAfter(event.getStartDateTime()) ? AttendanceStatus.LATE : AttendanceStatus.REGISTERED;
        AttendanceRecords record = AttendanceRecords.builder()
            .student(student)
            .event(event)
            .location(location)
            .timeIn(now)
            .attendanceStatus(initialStatus)
            .reason(now.isAfter(event.getStartDateTime()) ? "Late registration" : null)
            .build();

        attendanceRecordsRepository.save(record);
        log.info("Student {} {}registered for event {} with facial verification.", student.getStudentNumber(), now.isAfter(event.getStartDateTime()) ? "late " : "", event.getEventId());

        return registrationRequest;
    }

    /**
     * PRIVATE HELPERS
     */

    private void validateEventStatus(EventSessions event, LocalDateTime now) {
        EventStatus status = event.getEventStatus();

        if (status == EventStatus.UPCOMING) {
            throw new IllegalStateException(String.format("Registration not yet open. It starts at %s.", event.getTimeInRegistrationStartDateTime()));
        }

        if (status == EventStatus.CONCLUDED) {
            throw new IllegalStateException("This event has already ended and at the stage of finalizing records. You can no longer register.");
        }

        if (status == EventStatus.FINALIZED) {
            throw new IllegalStateException("The event has already ended and records were finalized.");
        }
    }

    private void verifyStudentFace(String studentNumber, String faceImageBase64) {
        try {
            BiometricData biometricData = biometricsRepository
                .findByStudentNumber(studentNumber)
                .orElseThrow(() -> new IllegalStateException("No biometric data found for student. Please register your face first."));

            if (biometricData.getFacialEncoding() == null || biometricData.getFacialEncoding().isEmpty()) {
                throw new IllegalStateException("Student's facial encoding is not registered");
            }

            log.info("Extracting facial encoding from uploaded image for student: {}", studentNumber);
            EventRegistrationBiometricsVerificationResponse encodingResponse = biometricsVerificationService.extractFaceEncoding(faceImageBase64);

            if (!encodingResponse.getSuccess() || encodingResponse.getFacialEncoding() == null) {
                throw new IllegalStateException("Failed to detect face in uploaded image");
            }

            log.info("Comparing facial encodings for student: {}", studentNumber);
            BiometricsVerificationResponse verificationResponse = biometricsVerificationService.verifyFace(encodingResponse.getFacialEncoding(), biometricData.getFacialEncoding());

            if (!verificationResponse.getSuccess() || !verificationResponse.getIs_face_matched()) {
                log.warn("Facial verification failed for student: {}", studentNumber);
                throw new IllegalStateException("Facial verification failed. Please try again with better lighting.");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during facial verification for student {}: {}", studentNumber, e.getMessage());
            throw new IllegalStateException("Facial verification error: " + e.getMessage());
        }
    }

    private void isAlreadyRegistered(Students student, EventSessions event, EventLocations location) {
        List<AttendanceRecords> existingRecords = attendanceRecordsRepository.findByStudentAndEventAndLocationAndAttendanceStatus(student, event, location, AttendanceStatus.REGISTERED);
        if (!existingRecords.isEmpty()) {
            throw new IllegalStateException("Student is already checked in for this event/location.");
        }
    }

    private boolean isStudentEligibleForEvent(EventSessions event, Students student) {
        EligibilityCriteria criteria = event.getEligibleStudents();
        if (criteria == null || criteria.isAllStudents()) return true;
        if (student.getSection() == null) {
            log.warn("Student {} has no section—cannot evaluate eligibility for event {}", student.getStudentNumber(), event.getEventId());
            return false;
        }
        Courses course = student.getSection().getCourse();
        Clusters cluster = (course != null) ? course.getCluster() : null;
        if (criteria.getSections() != null && criteria.getSections().contains(student.getSection().getId())) {
            log.debug("Student {} eligible via section match for event {}", student.getStudentNumber(), event.getEventId());
            return true;
        }
        if (criteria.getCourse() != null && course != null && criteria.getCourse().contains(course.getId())) {
            log.debug("Student {} eligible via course match for event {}", student.getStudentNumber(), event.getEventId());
            return true;
        }
        if (criteria.getCluster() != null && cluster != null && criteria.getCluster().contains(cluster.getClusterId())) {
            log.debug("Student {} eligible via cluster match for event {}", student.getStudentNumber(), event.getEventId());
            return true;
        }
        log.debug("Student {} not eligible for event {} (no matches)", student.getStudentNumber(), event.getEventId());
        return false;
    }
}
