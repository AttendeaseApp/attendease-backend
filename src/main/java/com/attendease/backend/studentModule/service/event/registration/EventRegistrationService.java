package com.attendease.backend.studentModule.service.event.registration;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EligibleAttendees.EligibilityCriteria;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.EventCheckIn.RegistrationRequest;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.studentModule.dto.response.biometrics.FaceEncodingResponse;
import com.attendease.backend.studentModule.dto.response.biometrics.FaceVerificationResponse;
import com.attendease.backend.studentModule.service.utils.LocationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventRegistrationService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final LocationRepository eventLocationsRepository;
    private final BiometricsVerificationService biometricsVerificationService;
    private final BiometricsRepository biometricsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;
//    private final StudentRepository studentRepository;
    private final LocationValidator locationValidator;

    public RegistrationRequest eventRegistration(String authenticatedUserId, RegistrationRequest registrationRequest) {
        Users user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentsRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
        EventSessions event = eventSessionsRepository.findById(registrationRequest.getEventId()).orElseThrow(() -> new IllegalStateException("Event not found"));

        LocalDateTime now = LocalDateTime.now();
        getEventStatus(event, now);

//        if (!isStudentEligibleForEvent(event, student)) {
//            throw new IllegalStateException("Student is not eligible to check in for this event.");
//        }

        EventLocations location = eventLocationsRepository.findById(registrationRequest.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));

        if (!locationValidator.isWithinLocationBoundary(location, registrationRequest.getLatitude(), registrationRequest.getLongitude())) {
            throw new IllegalStateException("Student is outside the event location boundary");
        }

        isAlreadyRegistered(student, event, location);

        if (registrationRequest.getFaceImageBase64() == null || registrationRequest.getFaceImageBase64().isEmpty()) {
            throw new IllegalStateException("Face image is required for check-in");
        }

        verifyStudentFace(student.getStudentNumber(), registrationRequest.getFaceImageBase64());

        AttendanceRecords record = AttendanceRecords.builder()
                .student(student)
                .event(event)
                .location(location)
                .timeIn(now)
                .attendanceStatus(AttendanceStatus.REGISTERED)
                .build();

        attendanceRecordsRepository.save(record);
        log.info("Student {} successfully registered for event {} with facial verification.", student.getStudentNumber(), event.getEventId());

        return registrationRequest;
    }

    private void getEventStatus(EventSessions event, LocalDateTime now) {
        EventStatus status = event.getEventStatus();

        if (status == EventStatus.UPCOMING) {
            throw new IllegalStateException(String.format("Registration not yet open. It starts at %s.", event.getTimeInRegistrationStartDateTime()));
        }

        if (status == EventStatus.CONCLUDED) {
            throw new IllegalStateException("This event has already ended and at the stage of finalizing records. You can no longer register.");
        }

        if (status == EventStatus.ONGOING && now.isAfter(event.getStartDateTime())) {
            throw new IllegalStateException("The event has already started. You can no longer register.");
        }

        if (status == EventStatus.FINALIZED) {
            throw new IllegalStateException("The event has already ended and records were finalized.");
        }
    }

    private void verifyStudentFace(String studentNumber, String faceImageBase64) {
        try {
            BiometricData biometricData = biometricsRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalStateException("No biometric data found for student. Please register your face first."));

            if (biometricData.getFacialEncoding() == null || biometricData.getFacialEncoding().isEmpty()) {
                throw new IllegalStateException("Student's facial encoding is not registered");
            }

            log.info("Extracting facial encoding from uploaded image for student: {}", studentNumber);
            FaceEncodingResponse encodingResponse = biometricsVerificationService.extractFaceEncoding(faceImageBase64);

            if (!encodingResponse.getSuccess() || encodingResponse.getFacialEncoding() == null) {
                throw new IllegalStateException("Failed to detect face in uploaded image");
            }

            log.info("Comparing facial encodings for student: {}", studentNumber);
            FaceVerificationResponse verificationResponse = biometricsVerificationService.verifyFace(encodingResponse.getFacialEncoding(), biometricData.getFacialEncoding());

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
        if (criteria == null) return false;

        if (criteria.isAllStudents()) return true;

        String studentCourseId = student.getCourseId();
        String studentSectionId = student.getSectionId();

        boolean courseMatch = criteria.getCourse() != null && criteria.getCourse().contains(studentCourseId);
        boolean sectionMatch = criteria.getSections() != null && criteria.getSections().contains(studentSectionId);

        return courseMatch || sectionMatch;
    }
}

