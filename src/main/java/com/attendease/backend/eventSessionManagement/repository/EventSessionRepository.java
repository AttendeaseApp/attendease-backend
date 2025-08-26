package com.attendease.backend.eventSessionManagement.repository;

import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.enums.EventStatus;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@Slf4j
public class EventSessionRepository {

    private static final String COLLECTION_NAME = "eventSessions";

    private final Firestore firestore;

    public EventSessionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String save(EventSessions eventSession) throws ExecutionException, InterruptedException {
        CollectionReference collection = firestore.collection(COLLECTION_NAME);
        DocumentReference docRef;
        if (eventSession.getEventId() == null || eventSession.getEventId().isEmpty()) {
            docRef = collection.document();
            eventSession.setEventId(docRef.getId());
            eventSession.setCreatedAt(new Date());
            eventSession.setUpdatedAt(new Date());
            ApiFuture<WriteResult> writeResult = docRef.set(eventSession);
            writeResult.get();
            log.info("Created new event session with ID: {}", eventSession.getEventId());
        } else {
            docRef = collection.document(eventSession.getEventId());
            eventSession.setUpdatedAt(new Date());
            ApiFuture<WriteResult> writeResult = docRef.set(eventSession, SetOptions.merge());
            writeResult.get();
            log.info("Updated event session with ID: {}", eventSession.getEventId());
        }
        return eventSession.getEventId();
    }

    public Optional<EventSessions> findById(String eventId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(eventId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            EventSessions eventSession = document.toObject(EventSessions.class);
            log.info("Found event session with ID: {}", eventId);
            assert eventSession != null;
            return Optional.of(eventSession);
        } else {
            log.warn("Event session not found with ID: {}", eventId);
            return Optional.empty();
        }
    }

    public List<EventSessions> findAll() throws ExecutionException, InterruptedException {
        CollectionReference collection = firestore.collection(COLLECTION_NAME);
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

    public List<EventSessions> findByStatus(EventStatus status) throws ExecutionException, InterruptedException {
        CollectionReference collection = firestore.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> querySnapshot = collection.whereEqualTo("eventStatus", status).orderBy("createdAt", Query.Direction.DESCENDING).get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<EventSessions> eventSessions = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            EventSessions eventSession = document.toObject(EventSessions.class);
            eventSessions.add(eventSession);
        }
        log.info("Retrieved {} event sessions with status: {}", eventSessions.size(), status);
        return eventSessions;
    }

    public List<EventSessions> findByDateRange(Date dateFrom, Date dateTo) throws ExecutionException, InterruptedException {
        CollectionReference collection = firestore.collection(COLLECTION_NAME);
        Query query = collection.orderBy("startDateTime");
        if (dateFrom != null) {
            query = query.whereGreaterThanOrEqualTo("startDateTime", dateFrom);
        }
        if (dateTo != null) {
            query = query.whereLessThanOrEqualTo("startDateTime", dateTo);
        }

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<EventSessions> eventSessions = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            EventSessions eventSession = document.toObject(EventSessions.class);
            eventSessions.add(eventSession);
        }
        log.info("Retrieved {} event sessions in date range {} to {}", eventSessions.size(), dateFrom, dateTo);
        return eventSessions;
    }

    public List<EventSessions> findByStatusAndDateRange(EventStatus status, Date dateFrom, Date dateTo) throws ExecutionException, InterruptedException {
        CollectionReference collection = firestore.collection(COLLECTION_NAME);
        Query query = collection.whereEqualTo("eventStatus", status).orderBy("startDateTime");
        if (dateFrom != null) {
            query = query.whereGreaterThanOrEqualTo("startDateTime", dateFrom);
        }
        if (dateTo != null) {
            query = query.whereLessThanOrEqualTo("startDateTime", dateTo);
        }

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        List<EventSessions> eventSessions = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            EventSessions eventSession = document.toObject(EventSessions.class);
            eventSessions.add(eventSession);
        }
        log.info("Retrieved {} event sessions with status {} in date range {} to {}", eventSessions.size(), status, dateFrom, dateTo);
        return eventSessions;
    }

    public void deleteById(String eventId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(eventId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get();
        log.info("Deleted event session with ID: {}", eventId);
    }

    public boolean existsById(String eventId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(eventId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        boolean exists = document.exists();
        log.debug("Event session with ID {} exists: {}", eventId, exists);
        return exists;
    }
}
