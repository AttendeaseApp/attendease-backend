package com.attendease.backend.osaModule.service.management.location;

import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.locations.Geofencing.Geometry;
import com.attendease.backend.domain.locations.Request.EventLocationRequest;
import com.attendease.backend.domain.locations.Response.LocationResponse;
import com.attendease.backend.repository.locations.LocationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@link LocationsManagementService} is a service for managing event locations, including creation, retrieval,
 * and deletion of location entities with geospatial data support.
 *
 * <p>Handles operations on {@link EventLocations} such as building GeoJson polygons from requests,
 * calculating centroids for response DTOs, and enforcing validation on geometry types.</p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 * @since 2025-09-16
 */
@Service
@RequiredArgsConstructor
public class LocationsManagementService {

    private final LocationRepository locationRepository;

    /**
     * Creates a new event location based on the provided request.
     * Converts the geometry to a GeoJsonPolygon and sets initial timestamps.
     *
     * @param request the {@link EventLocationRequest} containing location details and geometry
     * @return a {@link LocationResponse} representing the created location with computed centroid
     * @throws ResponseStatusException if the geometry is invalid or not a Polygon
     */
    public LocationResponse createLocation(EventLocationRequest request) {
        GeoJsonPolygon polygon = convertToGeoJsonPolygon(request.getGeoJsonData());

        EventLocations location = EventLocations.builder()
            .locationName(request.getLocationName())
            .locationType(request.getLocationType())
            .geometryType("Polygon")
            .geometry(polygon)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        location = locationRepository.save(location);

        return convertToResponseDTO(location);
    }

    /**
     * Updates an existing event location based on the provided request.
     * Supports partial updates for name, type, and geometry. Updates the updatedAt timestamp.
     *
     * @param locationId the unique ID of the location to update
     * @param request the {@link EventLocationRequest} containing updated location details and optional geometry
     * @return a {@link LocationResponse} representing the updated location with computed centroid
     * @throws ResponseStatusException if the location is not found, or if the geometry is invalid or not a Polygon
     */
    public LocationResponse updateLocation(String locationId, EventLocationRequest request) {
        Optional<EventLocations> optLocation = locationRepository.findById(locationId);
        if (optLocation.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found: " + locationId);
        }
        EventLocations location = optLocation.get();
        if (request.getLocationName() != null) {
            location.setLocationName(request.getLocationName());
        }
        if (request.getLocationType() != null) {
            location.setLocationType(request.getLocationType());
        }
        if (request.getGeoJsonData() != null) {
            GeoJsonPolygon polygon = convertToGeoJsonPolygon(request.getGeoJsonData());
            location.setGeometry(polygon);
            location.setGeometryType("Polygon");
        }
        location.setUpdatedAt(LocalDateTime.now());
        location = locationRepository.save(location);
        return convertToResponseDTO(location);
    }

    private GeoJsonPolygon convertToGeoJsonPolygon(Geometry geometry) {
        if (geometry == null || !"Polygon".equalsIgnoreCase(geometry.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Polygon geometry is supported");
        }

        List<List<Double>> outerRing = geometry.getCoordinates().getFirst();

        List<Point> points = outerRing
            .stream()
            .map(coord -> new Point(coord.getFirst(), coord.get(1)))
            .toList();

        return new GeoJsonPolygon(points);
    }

    /**
     * Retrieves all event locations.
     *
     * @return a list of {@link LocationResponse} DTOs for all locations, including computed centroids
     * @throws ExecutionException if an execution error occurs during retrieval
     * @throws InterruptedException if the thread is interrupted during retrieval
     */
    public List<LocationResponse> getAllLocations() throws ExecutionException, InterruptedException {
        List<EventLocations> locations = locationRepository.findAll();
        return locations.stream().map(this::convertToResponseDTO).toList();
    }

    /**
     * Deletes an event location by its unique identifier.
     *
     * @param locationId the unique ID of the location to delete
     * @throws ResponseStatusException if the location is not found
     * @throws ExecutionException if an execution error occurs during deletion
     * @throws InterruptedException if the thread is interrupted during deletion
     */
    public void deleteLocationById(String locationId) throws ExecutionException, InterruptedException {
        boolean exists = locationRepository.existsById(locationId);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found: " + locationId);
        }

        locationRepository.deleteById(locationId);
    }

    private LocationResponse convertToResponseDTO(EventLocations location) {
        if (location == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Location data is null");
        }

        LocationResponse response = new LocationResponse();
        response.setLocationId(location.getLocationId());
        response.setLocationName(location.getLocationName());
        response.setLocationType(location.getLocationType());
        response.setCreatedAt(location.getCreatedAt());
        response.setUpdatedAt(location.getUpdatedAt());

        GeoJsonPolygon polygon = location.getGeometry();

        if (polygon != null) {
            List<GeoJsonLineString> lineStrings = polygon.getCoordinates();

            if (!lineStrings.isEmpty()) {
                GeoJsonLineString outerRing = lineStrings.getFirst();

                if (outerRing != null) {
                    List<Point> points = outerRing.getCoordinates();

                    if (!points.isEmpty()) {
                        double sumLat = 0.0;
                        double sumLng = 0.0;
                        int count = points.size();

                        if (points.getFirst().equals(points.get(count - 1))) {
                            count--;
                        }

                        for (int i = 0; i < count; i++) {
                            Point point = points.get(i);
                            sumLng += point.getX(); // longitude
                            sumLat += point.getY(); // latitude
                        }

                        double centroidLat = sumLat / count;
                        double centroidLng = sumLng / count;

                        response.setLatitude(centroidLat);
                        response.setLongitude(centroidLng);
                    }
                }
            }
        }

        return response;
    }
}
