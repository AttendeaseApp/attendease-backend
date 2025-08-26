package com.attendease.backend.eventLocationManagement.service;

import com.attendease.backend.eventLocationManagement.dto.LocationCreateGeoJsonDTO;
import com.attendease.backend.eventLocationManagement.dto.response.LocationResponseDTO;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface LocationServiceInterface {

    /**
     * Creates a new event location from a GeoJSON DTO object.
     *
     * @param createRequest the GeoJSON DTO containing location data
     * @return a response DTO representing the created location
     * @throws ExecutionException if the Firestore operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    LocationResponseDTO createLocationFromGeoJson(LocationCreateGeoJsonDTO createRequest) throws ExecutionException, InterruptedException;

    /**
     * Retrieves all event locations.
     *
     * @return a list of response DTOs representing all event locations
     * @throws ExecutionException if the Firestore operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    List<LocationResponseDTO> getAllLocations() throws ExecutionException, InterruptedException;

    /**
     * Deletes an event location by its document ID.
     *
     * @param locationId the ID of the location to delete
     * @throws ExecutionException if the Firestore operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    void deleteLocationById(String locationId) throws ExecutionException, InterruptedException;
}
