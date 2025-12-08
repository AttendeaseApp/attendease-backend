package com.attendease.backend.osa.service.management.location;

import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.locations.Request.EventLocationRequest;
import com.attendease.backend.domain.locations.Response.LocationResponse;
import com.attendease.backend.osa.service.management.location.impl.ManagementLocationServiceImpl;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * {@link ManagementLocationServiceImpl} is a service for managing event locations, including creation, retrieval,
 * and deletion of location entities with geospatial data support.
 *
 * <p>Handles operations on {@link EventLocations} such as building GeoJson polygons from requests,
 * calculating centroids for response DTOs, and enforcing validation on geometry types.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
public interface ManagementLocationService {

    /**
     * {@code createNewLocation} is used to create a new event location based on the provided request.
     * Converts the geometry to a GeoJsonPolygon and sets initial timestamps.
     *
     * @param request the {@link EventLocationRequest} containing location details and geometry
     * @return a {@link LocationResponse} representing the created location with computed centroid
     * @throws ResponseStatusException if the geometry is invalid or not a Polygon
     */
    LocationResponse createNewLocation(EventLocationRequest request);

    /**
     * {@code updateLocation} is used to update an existing event location based on the provided request.
     * Supports partial updates for name, type, and geometry. Updates the updatedAt timestamp.
     *
     * @param locationId the unique ID of the location to update
     * @param request the {@link EventLocationRequest} containing updated location details and optional geometry
     * @return a {@link LocationResponse} representing the updated location with computed centroid
     * @throws ResponseStatusException if the location is not found, or if the geometry is invalid or not a Polygon
     */
    LocationResponse updateLocation(String locationId, EventLocationRequest request);

    /**
     * {@code getAllLocations} is used to retrieve all event locations.
     *
     * @return a list of {@link LocationResponse} DTOs for all locations, including computed centroids
     */
    List<LocationResponse> getAllLocations();

    /**
     * {@code deleteLocationById} is used to delete an event location by its unique identifier.
     *
     * @param locationId the unique ID of the location to delete
     */
    void deleteLocationById(String locationId);
}
