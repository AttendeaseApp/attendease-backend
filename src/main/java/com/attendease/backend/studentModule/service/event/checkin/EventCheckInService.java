package com.attendease.backend.studentModule.service.event.checkin;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.records.EventCheckIn.EventCheckIn;
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
import com.attendease.backend.studentModule.service.authentication.biometrics.FacialRecognitionService;
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
    private final FacialRecognitionService facialRecognitionService;
    private final BiometricsRepository biometricsRepository;
    private final StudentRepository studentsRepository;
    private final UserRepository userRepository;

    public EventCheckIn checkInStudent(String authenticatedUserId, EventCheckIn eventCheckIn) {
        Users user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentsRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
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
      
//        if (!isStudentEligibleForEvent(event, student)) {
//           throw new IllegalStateException("Student is not eligible to check in for this event.");
//        }

        EventLocations location = eventLocationsRepository.findById(eventCheckIn.getLocationId()).orElseThrow(() -> new IllegalStateException("Event location not found"));

        double studentLat = eventCheckIn.getLatitude();
        double studentLon = eventCheckIn.getLongitude();

        if (!isWithinLocationBoundary(location, studentLat, studentLon)) {
            throw new IllegalStateException("Student is outside the event location boundary");
        }

        isAlreadyCheckedIn(student, event, location);

        if (eventCheckIn.getFaceImageBase64() == null || eventCheckIn.getFaceImageBase64().isEmpty()) {
            throw new IllegalStateException("Face image is required for check-in");
        }

        verifyStudentFace(student.getStudentNumber(), eventCheckIn.getFaceImageBase64());

        AttendanceRecords record = AttendanceRecords.builder()
                .student(student)
                .event(event)
                .location(location)
                .timeIn(null)
                .attendanceStatus(AttendanceStatus.CHECKED_IN)
                .build();

        attendanceRecordsRepository.save(record);
        log.info("Student {} successfully checked in to event {} with facial verification", student.getStudentNumber(), event.getEventId());

        return eventCheckIn;
    }

    private void verifyStudentFace(String studentNumber, String faceImageBase64) {
        try {
            BiometricData biometricData = biometricsRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalStateException("No biometric data found for student. Please register your face first."));

            if (biometricData.getFacialEncoding() == null || biometricData.getFacialEncoding().isEmpty()) {
                throw new IllegalStateException("Student's facial encoding is not registered");
            }

            log.info("Extracting facial encoding from uploaded image for student: {}", studentNumber);
            FaceEncodingResponse encodingResponse = facialRecognitionService.extractFaceEncoding(faceImageBase64);

            if (!encodingResponse.getSuccess() || encodingResponse.getFacialEncoding() == null) {
                throw new IllegalStateException("Failed to detect face in uploaded image");
            }

            log.info("Comparing facial encodings for student: {}", studentNumber);
            FaceVerificationResponse verificationResponse =
                    facialRecognitionService.verifyFace(
                            encodingResponse.getFacialEncoding(),
                            biometricData.getFacialEncoding()
                    );

            if (!verificationResponse.getSuccess() || !verificationResponse.getIs_face_matched()) {
                log.warn("Facial verification failed for student: {}. Confidence: {}, Distance: {}",
                        studentNumber,
                        verificationResponse.getConfidence(),
                        verificationResponse.getFace_distance()
                );
                throw new IllegalStateException(
                        String.format("Facial verification failed. Confidence: %.2f%%. Please try again with better lighting.", verificationResponse.getConfidence() * 100)
                );
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during facial verification for student {}: {}", studentNumber, e.getMessage());
            throw new IllegalStateException("Facial verification error: " + e.getMessage());
        }
    }

    private void isAlreadyCheckedIn(Students student, EventSessions event, EventLocations location) {
        List<AttendanceRecords> existingRecords = attendanceRecordsRepository.findByStudentAndEventAndLocationAndAttendanceStatus(student, event, location, AttendanceStatus.CHECKED_IN);

        if (!existingRecords.isEmpty()) {
            throw new IllegalStateException("Student is already checked in for this event/location.");
        }
    }

//    private boolean isStudentEligibleForEvent(EventSessions event, Students student) {
//        EligibilityCriteria criteria = event.getEligibleStudents();
//        if (criteria == null) return false;
//
//        if (criteria.isAllStudents()) return true;
//
//        String studentCourseId = student.getCourseId();
//        String studentSectionId = student.getSectionId();
//
//        boolean courseMatch = criteria.getCourse() != null && criteria.getCourse().contains(studentCourseId);
//        boolean sectionMatch = criteria.getSections() != null && criteria.getSections().contains(studentSectionId);
//
//        return courseMatch || sectionMatch;
//    }

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

