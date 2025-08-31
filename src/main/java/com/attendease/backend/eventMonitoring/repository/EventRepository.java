package com.attendease.backend.eventMonitoring.repository;

import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.records.AttendanceRecords;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
public class EventRepository implements EventRepositoryInterface {

    private static final String EVENT_SESSION_COLLECTIONS = "eventSessions";
    private static final String ATTENDANCE_RECORDS_COLLECTIONS = "attendanceRecords";
    private static final String EVENT_SESSION_COLLECTION_REFERENCE_ID = "eventRefId";

    private static final String EVENT_STATUS_DOCUMENT_NAME = "eventStatus";
    private final Firestore firestore;

    public EventRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<EventSessions> findOngoingEvents() {
        try {
            return firestore.collection(EVENT_SESSION_COLLECTIONS)
                    .whereIn(EVENT_STATUS_DOCUMENT_NAME, Arrays.asList("ONGOING", "ACTIVE"))
                    .get().get().toObjects(EventSessions.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching ongoing events", e);
        }
    }

    @Override
    public List<EventSessions> findAll() throws ExecutionException, InterruptedException {
        CollectionReference collection = firestore.collection(EVENT_SESSION_COLLECTIONS);
        ApiFuture<QuerySnapshot> querySnapshot = collection.orderBy("createdAt", Query.Direction.DESCENDING).get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<EventSessions> eventSessions = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            EventSessions eventSession = document.toObject(EventSessions.class);
            eventSessions.add(eventSession);
        }
        log.info("Retrieved {} event sessions", eventSessions.size());
        return eventSessions;
    }

    @Override
    public EventSessions findById(String eventId) {
        try {
            return firestore.collection(EVENT_SESSION_COLLECTIONS).document(eventId).get().get().toObject(EventSessions.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching event by ID", e);
        }
    }

    public void saveAttendanceRecord(AttendanceRecords record) {
        try {
            firestore.collection(ATTENDANCE_RECORDS_COLLECTIONS).document().set(record).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving attendance record", e);
        }
    }

    public List<AttendanceRecords> getAttendanceRecords(String eventId) {
        try {
            return firestore.collection(ATTENDANCE_RECORDS_COLLECTIONS).whereEqualTo(EVENT_SESSION_COLLECTION_REFERENCE_ID, firestore.collection(EVENT_SESSION_COLLECTIONS).document(eventId))
                .get().get().toObjects(AttendanceRecords.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching attendance records", e);
        }
    }
}
