package com.attendease.backend.osa.controller.management.location;

import com.attendease.backend.osa.service.management.location.ManagementLocationService;
import com.attendease.backend.domain.locations.Request.EventLocationRequest;
import com.attendease.backend.domain.locations.Response.LocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code ManagementLocationController} is used for managing event locations, providing endpoints for CRUD operations on event locations.
 * All endpoints are secured and accessible only to users with the 'OSA' role.
 *
 * <p>Handles requests related to creating, updating, retrieving, and deleting event locations with geospatial data.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-16
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class ManagementLocationController {

    private final ManagementLocationService managementLocationService;

    /**
     * Creates a new event location based on the provided request data.
     *
     * <p>This endpoint accepts a JSON payload containing location name, type, and GeoJSON polygon geometry.
     * The geometry is validated to ensure it is a Polygon type. Upon success, the location is persisted
     * and a response is returned with computed centroid coordinates (latitude and longitude).</p>
     *
     * @param request the {@link EventLocationRequest} containing details such as location name, type, and geometry
     * @return the {@link LocationResponse} representing the newly created location, including centroid
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse createLocation(@RequestBody EventLocationRequest request) {
        return managementLocationService.createNewLocation(request);
    }

    /**
     * Updates an existing event location identified by its ID.
     *
     * <p>This endpoint supports partial updates: only provided fields (name, type, or geometry) are modified.
     * If geometry is included, it must be a valid Polygon and will replace the existing one. The updated
     * timestamp is automatically set, and the response includes the recomputed centroid.</p>
     *
     * @param locationId the unique identifier of the event location to update
     * @param request the {@link EventLocationRequest} containing the fields to update (partial updates supported)
     * @return the {@link LocationResponse} representing the updated location, including centroid
     */
    @PatchMapping("/{locationId}")
    public LocationResponse updateLocation(@PathVariable String locationId, @RequestBody EventLocationRequest request) {
        return managementLocationService.updateLocation(locationId, request);
    }

    /**
     * Retrieves all event locations stored in the system.
     *
     * <p>Returns a list of all locations with their details, including computed centroid coordinates for each.
     * Suitable for listing or dashboard views.</p>
     *
     * @return a {@link List} of {@link LocationResponse} objects, each containing location details and centroid
     */
    @GetMapping
    public List<LocationResponse> getAllLocations() {
        return managementLocationService.getAllLocations();
    }

    /**
     * Deletes an event location by its unique identifier.
     *
     * <p>Permanently removes the location from the database. No response body is returned on success.</p>
     *
     * @param locationId the unique identifier of the event location to delete
     */
    @DeleteMapping("/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable String locationId) {
        managementLocationService.deleteLocationById(locationId);
    }
}

