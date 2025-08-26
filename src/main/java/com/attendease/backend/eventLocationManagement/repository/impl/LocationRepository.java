package com.attendease.backend.eventLocationManagement.repository.impl;

import com.attendease.backend.eventLocationManagement.repository.LocationRepositoryInterface;
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
public class LocationRepository implements LocationRepositoryInterface {

    private final Firestore firestore;
    private static final String EVENT_LOCATION_COLLECTION = "eventLocations";

    public LocationRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String save(EventLocations location) throws ExecutionException, InterruptedException {
        CollectionReference locations = firestore.collection(EVENT_LOCATION_COLLECTION);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EventLocations> findById(String locationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(EVENT_LOCATION_COLLECTION).document(locationId);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EventLocations> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(EVENT_LOCATION_COLLECTION).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        return documents.stream()
                .map(doc -> {
                    EventLocations location = doc.toObject(EventLocations.class);
                    location.setLocationId(doc.getId());
                    return location;
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String locationId) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> result = firestore.collection(EVENT_LOCATION_COLLECTION).document(locationId).delete();
        result.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(String locationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(EVENT_LOCATION_COLLECTION).document(locationId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        return document.exists();
    }
}

