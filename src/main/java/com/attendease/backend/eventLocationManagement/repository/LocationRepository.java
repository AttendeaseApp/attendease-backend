package com.attendease.backend.eventLocationManagement.repository;

import com.attendease.backend.model.locations.EventLocations;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class LocationRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "eventLocations";

    @Autowired
    public LocationRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String save(EventLocations location) throws ExecutionException, InterruptedException {
        CollectionReference locations = firestore.collection(COLLECTION_NAME);

        DocumentReference docRef;
        if (location.getLocationId() == null || location.getLocationId().isEmpty()) {
            docRef = locations.document();
            location.setLocationId(docRef.getId());
            ApiFuture<WriteResult> result = docRef.set(location);
            result.get();
        } else {
            docRef = locations.document(location.getLocationId());
            ApiFuture<WriteResult> result = docRef.set(location);
            result.get();
        }
        return location.getLocationId();
    }

    public Optional<EventLocations> findById(String locationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(locationId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            EventLocations location = document.toObject(EventLocations.class);
            if (location != null) {
                location.setLocationId(document.getId());
            }
            return Optional.ofNullable(location);
        }
        return Optional.empty();
    }

    public List<EventLocations> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        return documents.stream()
                .map(doc -> {
                    EventLocations location = doc.toObject(EventLocations.class);
                    location.setLocationId(doc.getId());
                    return location;
                })
                .collect(Collectors.toList());
    }

    public void deleteById(String locationId) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME).document(locationId).delete();
        result.get();
    }

    public boolean existsById(String locationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(locationId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        return document.exists();
    }
}

