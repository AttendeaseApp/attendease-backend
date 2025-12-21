package com.attendease.backend.osa.controller.management.location.management;

import com.attendease.backend.osa.service.management.location.management.LocationManagementService;
import com.attendease.backend.domain.location.management.LocationManagementRequest;
import com.attendease.backend.domain.location.management.LocationManagementResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code ManagementLocationController} is used for managing event locations, providing endpoints for CRUD operations on event locations.
 * All endpoints are secured and accessible only to user with the 'osa' role.
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
public class LocationManagementController {

    private final LocationManagementService locationManagementService;

    /**
     * Creates a new event location based on the provided request data.
     *
     * <p>This endpoint accepts a JSON payload containing location name, type, and GeoJSON polygon geometry.
     * The geometry is validated to ensure it is a Polygon type. Upon success, the location is persisted
     * and a response is returned with computed centroid coordinates (latitude and longitude).</p>
     *
     * @param request the {@link LocationManagementRequest} containing details such as location name, type, and geometry
     * @return the {@link LocationManagementResponse} representing the newly created location, including centroid
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationManagementResponse createLocation(@Valid @RequestBody LocationManagementRequest request) {
        return locationManagementService.createNewLocation(request);
    }

    /**
     * Updates an existing event location identified by its ID.
     *
     * <p>This endpoint supports partial updates: only provided fields (name, type, or geometry) are modified.
     * If geometry is included, it must be a valid Polygon and will replace the existing one. The updated
     * timestamp is automatically set, and the response includes the recomputed centroid.</p>
     *
     * @param locationId the unique identifier of the event location to update
     * @param request the {@link LocationManagementRequest} containing the fields to update (partial updates supported)
     * @return the {@link LocationManagementResponse} representing the updated location, including centroid
     */
    @PatchMapping("/{locationId}")
    public LocationManagementResponse updateLocation(@PathVariable String locationId, @Valid @RequestBody LocationManagementRequest request) {
        return locationManagementService.updateLocation(locationId, request);
    }

    /**
     * Retrieves all event locations stored in the system.
     *
     * <p>Returns a list of all locations with their details, including computed centroid coordinates for each.
     * Suitable for listing or dashboard views.</p>
     *
     * @return a {@link List} of {@link LocationManagementResponse} objects, each containing location details and centroid
     */
    @GetMapping
    public List<LocationManagementResponse> getAllLocations() {
        return locationManagementService.getAllLocations();
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
        locationManagementService.deleteLocationById(locationId);
    }
}

