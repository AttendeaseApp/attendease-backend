package com.attendease.backend.attendanceTrackingService.service.impl;

import com.attendease.backend.attendanceTrackingService.service.EventCheckInServiceInterface;
import com.attendease.backend.eventMonitoring.dto.EventCheckInDto;
import com.attendease.backend.eventMonitoring.repository.EventRepositoryInterface;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.locations.EventLocations;
import com.attendease.backend.model.locations.GeofenceData;
import com.attendease.backend.model.records.AttendanceRecords;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

/*
 * Handles the logic for student event check-in, including eligibility, geofence validation, and duplicate check-in prevention.
 */
@Slf4j
@Service
public class EventCheckInService implements EventCheckInServiceInterface {

    private static final String EVENT_SESSION_COLLECTIONS = "eventSessions";
    private static final String EVENT_LOCATIONS_COLLECTIONS = "eventLocations";
    private static final String ATTENDANCE_RECORDS_COLLECTIONS = "attendanceRecords";
    private final EventRepositoryInterface eventRepository;
    private final Firestore firestore;

    /*
     * Constructor for EventCheckInService.
     */
    public EventCheckInService(EventRepositoryInterface eventRepository, Firestore firestore) {
        this.eventRepository = eventRepository;
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
}
