package com.attendease.backend.eventLocationManagement.repository;

import com.attendease.backend.model.locations.EventLocations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface LocationRepositoryInterface {

    /**
     * Saves or updates an EventLocation entity in Firestore.
     *
     * @param location the EventLocations object to save or update
     * @return the ID of the saved location
     * @throws ExecutionException if the Firestore operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    String save(EventLocations location) throws ExecutionException, InterruptedException;

    /**
     * Finds a location by its Firestore document ID.
     *
     * @param locationId the ID of the location to retrieve
     * @return an Optional containing the found EventLocations, or empty if not found
     * @throws ExecutionException if the Firestore operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    Optional<EventLocations> findById(String locationId) throws ExecutionException, InterruptedException;

    /**
     * Retrieves all event locations from Firestore.
     *
     * @return a list of all EventLocations
     * @throws ExecutionException if the Firestore operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    List<EventLocations> findAll() throws ExecutionException, InterruptedException;

    /**
     * Deletes a location by its document ID.
     *
     * @param locationId the ID of the location to delete
     * @throws ExecutionException if the Firestore operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    void deleteById(String locationId) throws ExecutionException, InterruptedException;

    /**
     * Checks if a location exists by its document ID.
     *
     * @param locationId the ID to check
     * @return true if the document exists, false otherwise
     * @throws ExecutionException if the Firestore operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    boolean existsById(String locationId) throws ExecutionException, InterruptedException;
}
