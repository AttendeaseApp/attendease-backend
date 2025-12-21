package com.attendease.backend.osa.service.location.management;

import com.attendease.backend.domain.location.management.LocationManagementRequest;
import com.attendease.backend.domain.location.management.LocationManagementResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * {@link LocationManagementService} is the contract interface for implementation
 * service for managing event locations, including creation, retrieval,
 * and deletion of location entities with geospatial data support.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
public interface LocationManagementService {

    /**
     * {@code createNewLocation} is used to create a new event location based on the provided request.
     * Converts the geometry to a GeoJsonPolygon and sets initial timestamps.
     *
     * @param request the {@link LocationManagementRequest} containing location details and geometry
     * @return a {@link LocationManagementResponse} representing the created location with computed centroid
     * @throws ResponseStatusException if the geometry is invalid or not a Polygon
     */
    LocationManagementResponse createNewLocation(LocationManagementRequest request);

    /**
     * {@code updateLocation} is used to update an existing event location based on the provided request.
     * Supports partial updates for name, type, and geometry. Updates the updatedAt timestamp.
     *
     * @param locationId the unique ID of the location to update
     * @param request the {@link LocationManagementRequest} containing updated location details and optional geometry
     * @return a {@link LocationManagementResponse} representing the updated location with computed centroid
     * @throws ResponseStatusException if the location is not found, or if the geometry is invalid or not a Polygon
     */
    LocationManagementResponse updateLocation(String locationId, LocationManagementRequest request);

    /**
     * {@code getAllLocations} is used to retrieve all event locations.
     *
     * @return a list of {@link LocationManagementResponse} DTOs for all locations, including computed centroids
     */
    List<LocationManagementResponse> getAllLocations();

    /**
     * {@code deleteLocationById} is used to delete an event location by its unique identifier.
     *
     * @param locationId the unique ID of the location to delete
     */
    void deleteLocationById(String locationId);
}
