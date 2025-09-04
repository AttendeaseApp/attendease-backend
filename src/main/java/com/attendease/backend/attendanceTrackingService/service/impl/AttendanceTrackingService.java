package com.attendease.backend.attendanceTrackingService.service.impl;

import com.attendease.backend.attendanceTrackingService.dto.LocationInfoDto;
import com.attendease.backend.attendanceTrackingService.repository.AttendanceRepository;
import com.attendease.backend.attendanceTrackingService.service.AttendanceTrackingServiceInterface;
import com.attendease.backend.eventMonitoring.dto.EventCheckInDto;
import com.attendease.backend.eventMonitoring.repository.EventRepositoryInterface;
import com.attendease.backend.model.enums.AttendanceStatus;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.locations.EventLocations;
import com.attendease.backend.model.locations.GeofenceData;
import com.attendease.backend.model.records.AttendanceRecords;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/*
 * Handles the logic for student event check-in, including eligibility, geofence validation, and duplicate check-in prevention.
 */
@Slf4j
@Service
public class AttendanceTrackingService implements AttendanceTrackingServiceInterface {

    private final ConcurrentHashMap<String, LocationInfoDto> locationMap = new ConcurrentHashMap<>();
    private static final String EVENT_SESSION_COLLECTIONS = "eventSessions";
    private static final String EVENT_LOCATIONS_COLLECTIONS = "eventLocations";
    private static final String ATTENDANCE_RECORDS_COLLECTIONS = "attendanceRecords";
    private final EventRepositoryInterface eventRepository;
    public final AttendanceRepository attendanceRepository;
    private final Firestore firestore;

    /*
     * Constructor for AttendanceTrackingService.
     */
    public AttendanceTrackingService(EventRepositoryInterface eventRepository, AttendanceRepository attendanceRepository, Firestore firestore) {
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.firestore = firestore;
    }

    /*
     * Validates and processes student check-in for an event at a specific location.
     * Ensures the student is within geofence, and not already checked in.
     */
    @Override
    public EventCheckInDto checkInStudent(String studentNumber, EventCheckInDto checkInDTO) throws ExecutionException, InterruptedException {
        EventSessions event = eventRepository.findById(checkInDTO.getEventId());
        // TODO: verify student eligibility for event
        if (event == null) {
            throw new IllegalStateException("Student is not eligible for this event");
        }

        EventLocations location = firestore.collection(EVENT_LOCATIONS_COLLECTIONS)
                .document(checkInDTO.getLocationId()).get().get().toObject(EventLocations.class);
        if (location == null) {
            throw new IllegalStateException("Event location not found");
        }

        double studentLat = checkInDTO.getLatitude();
        double studentLon = checkInDTO.getLongitude();
        GeofenceData geofence = location.getGeofenceParameters();
        if (geofence == null) {
            throw new IllegalStateException("Geofence parameters not found for location");
        }
        if (!isWithinGeofence(studentLat, studentLon, geofence.getCenterLatitude(),
                geofence.getCenterLongitude(), geofence.getRadiusMeters())) {
            throw new IllegalStateException("Student is outside the event geofence");
        }

        isAlreadyCheckedIn(studentNumber, studentNumber, studentNumber);

        AttendanceRecords record = new AttendanceRecords();
        record.setStudentNumberRefId(firestore.collection("students").document(studentNumber));
        record.setEventRefId(firestore.collection(EVENT_SESSION_COLLECTIONS).document(checkInDTO.getEventId()));
        record.setLocationRefId(firestore.collection(EVENT_LOCATIONS_COLLECTIONS).document(checkInDTO.getLocationId()));
        record.setTimeIn(null);
        record.setAttendanceStatus(com.attendease.backend.model.enums.AttendanceStatus.PRESENT);

        eventRepository.saveAttendanceRecord(record);

        EventCheckInDto response = new EventCheckInDto();
        response.setEventId(checkInDTO.getEventId());
        response.setStudentNumber(studentNumber);
        response.setLocationId(checkInDTO.getLocationId());
        response.setLatitude(studentLat);
        response.setLongitude(studentLon);
        response.setCheckInTime(null);
        return response;
    }

    private void isAlreadyCheckedIn(String studentNumber, String eventId, String locationId) {
        List<AttendanceRecords> existingRecords = null;
        try {
            existingRecords = firestore.collection(ATTENDANCE_RECORDS_COLLECTIONS)
                    .whereEqualTo("studentNumberRefId", firestore.collection("students").document(studentNumber))
                    .whereEqualTo("eventRefId", firestore.collection(EVENT_SESSION_COLLECTIONS).document(eventId))
                    .whereEqualTo("locationRefId", firestore.collection(EVENT_LOCATIONS_COLLECTIONS).document(locationId))
                    .whereEqualTo("attendanceStatus", com.attendease.backend.model.enums.AttendanceStatus.PRESENT)
                    .get().get().toObjects(AttendanceRecords.class);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (existingRecords != null && !existingRecords.isEmpty()) {
            throw new IllegalStateException("Student is already checked in for this event/location.");
        }
    }

    private boolean isWithinGeofence(double lat1, double lon1, double lat2, double lon2, double radiusMeters) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        log.info("Calculated distance: {} meters (allowed radius: {})", distance, radiusMeters);
        return distance <= radiusMeters;
    }

    /**
     * Evaluates attendance status based on event timing and location presence.
     * Returns PRESENT, ABSENT, or null if status is not yet determinable.
     */
    public AttendanceStatus evaluateAttendance(EventSessions event, AttendanceRecords record, LocationInfoDto locationInfo) {
        Date now = new Date();
        Date eventStart = event.getStartDateTime();
        Date eventEnd = event.getEndDateTime();
        Date checkInTime = record.getTimeIn();

        log.debug("Evaluating attendance: studentTimeIn={}, eventStart={}, eventEnd={}, locationInfo={}",
                checkInTime, eventStart, eventEnd, locationInfo);

        // late check-in
        if (checkInTime != null && checkInTime.after(eventStart)) {
            log.info("Student was late for event. Marking as ABSENT.");
            return AttendanceStatus.ABSENT;
        }

        // left location for >10min before event end
        if (locationInfo.getLastExitTime() != null &&
                locationInfo.getLastExitTime().before(new Date(eventEnd.getTime() - 10 * 60 * 1000)) &&
                (locationInfo.getLastReturnTime() == null ||
                        locationInfo.getLastReturnTime().after(new Date(locationInfo.getLastExitTime().getTime() + 10 * 60 * 1000)))) {
            log.info("Student left early for more than 10 minutes. Marking as ABSENT.");
            return AttendanceStatus.ABSENT;
        }

        // stayed until event end
        if (locationInfo.isPresentAtLocation() && now.after(eventEnd)) {
            log.info("Student stayed at location until event end. Marking as PRESENT.");
            return AttendanceStatus.PRESENT;
        }

        log.debug("Attendance status not yet determinable. Continuing to monitor.");
        return null;
    }

    /**
     * Updates and evaluates attendance, then persists it to Firestore if status is determinable.
     * Returns a result object indicating success/failure and details.
     */
    public AttendanceSaveResult processAndPersistAttendance(
            String studentNumber,
            DocumentReference studentRef,
            DocumentReference eventRef,
            DocumentReference locationRef,
            DocumentReference updatedByUserRef,
            LocationInfoDto locationInfo,
            EventSessions event,
            AttendanceRecords existingRecord
    ) {
        AttendanceSaveResult result = new AttendanceSaveResult();
        try {
            log.info("Processing attendance for student [{}] and event [{}]", studentNumber, eventRef.getId());

            // Validate required fields
            if (studentRef == null || eventRef == null || locationRef == null || updatedByUserRef == null || locationInfo == null || event == null) {
                result.success = false;
                result.message = "Missing required data for attendance record.";
                log.warn(result.message);
                return result;
            }

            updateLocation(studentNumber, locationInfo);

            AttendanceStatus status = evaluateAttendance(event, existingRecord, locationInfo);

            if (status != null) {
                if (existingRecord.getAttendanceStatus() == null || !existingRecord.getAttendanceStatus().equals(status)) {
                    existingRecord.setStudentNumberRefId(studentRef);
                    existingRecord.setEventRefId(eventRef);
                    existingRecord.setLocationRefId(locationRef);
                    existingRecord.setAttendanceStatus(status);
                    existingRecord.setUpdatedByUserRefId(updatedByUserRef);
                    existingRecord.setTimeOut(new Date());

                    log.info("Saving attendance record: {}", existingRecord);
                    try {
                        attendanceRepository.saveAttendance(existingRecord);
                        result.success = true;
                        result.message = "Attendance record saved.";
                        result.savedRecord = existingRecord;
                        log.info("Attendance record saved for student [{}]", studentNumber);
                    } catch (Exception dbEx) {
                        result.success = false;
                        result.message = "Database save failed: " + dbEx.getMessage();
                        log.error(result.message, dbEx);
                    }
                } else {
                    result.success = false;
                    result.message = "Attendance status unchanged. No update performed.";
                    log.info(result.message);
                }
            } else {
                result.success = false;
                result.message = "Attendance status not final. Record will not be saved yet.";
                log.debug(result.message);
            }

        } catch (Exception e) {
            result.success = false;
            result.message = "Failed to process attendance: " + e.getMessage();
            log.error(result.message, e);
        }
        return result;
    }

    /**
     * Result object for attendance save operation.
     */
    public static class AttendanceSaveResult {
        public boolean success;
        public String message;
        public AttendanceRecords savedRecord;
    }

    public AttendanceRecords getExistingAttendance(DocumentReference studentRef, DocumentReference eventRef) {
        try {
            log.debug("Fetching existing attendance for studentRef [{}], eventRef [{}]",studentRef.getPath(), eventRef.getPath());

            AttendanceRecords record = attendanceRepository.getAttendanceByStudentAndEvent(studentRef, eventRef);

            if (record != null) {
                log.info("Existing attendance found for student [{}] and event [{}]", studentRef.getId(), eventRef.getId());
            } else {
                log.info("No existing attendance found for student [{}] and event [{}]", studentRef.getId(), eventRef.getId());
            }

            return record;

        } catch (Exception e) {
            log.error("Error retrieving existing attendance record: {}", e.getMessage(), e);
            return null;
        }
    }

    public void updateLocation(String studentNumber, LocationInfoDto locationInfo) {
        log.debug("Updating location for student [{}] with info: {}", studentNumber, locationInfo);
        locationMap.put(studentNumber, locationInfo);
    }
}
