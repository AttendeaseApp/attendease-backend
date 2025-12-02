package com.attendease.backend.osaModule.service.management.location;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.locations.Geofencing.Geometry;
import com.attendease.backend.domain.locations.Request.EventLocationRequest;
import com.attendease.backend.domain.locations.Response.LocationResponse;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationsManagementService {

    private final LocationRepository locationRepository;
    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    /**
     * Creates a new event location based on the provided request.
     * Converts the geometry to a GeoJsonPolygon and sets initial timestamps.
     *
     * @param request the {@link EventLocationRequest} containing location details and geometry
     * @return a {@link LocationResponse} representing the created location with computed centroid
     * @throws ResponseStatusException if the geometry is invalid or not a Polygon
     */
    public LocationResponse createLocation(EventLocationRequest request) {
        if (request.getLocationName() == null || request.getLocationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Location name is required and cannot be blank");
        }
        String trimmedName = request.getLocationName().trim();
        Optional<EventLocations> existing = locationRepository.findByLocationName(trimmedName);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location with name '" + trimmedName + "' already exists");
        }
        if (request.getLocationType() == null) {
            throw new IllegalArgumentException("Location type is required");
        }
        if (request.getGeoJsonData() == null) {
            throw new IllegalArgumentException("Geometry data is required");
        }
        GeoJsonPolygon polygon = convertToGeoJsonPolygon(request.getGeoJsonData());

        EventLocations location = EventLocations.builder()
                .locationName(trimmedName)
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

        List<EventSessions> referencingEvents = eventSessionsRepository.findByEventLocationId(locationId);
        Set<EventStatus> blockedStatuses = Set.of(EventStatus.REGISTRATION, EventStatus.ONGOING, EventStatus.CONCLUDED, EventStatus.FINALIZED);

        Map<EventStatus, Long> blockedCounts = referencingEvents.stream().filter(event -> blockedStatuses.contains(event.getEventStatus()))
                .collect(Collectors.groupingBy(EventSessions::getEventStatus, Collectors.counting()));

        if (!blockedCounts.isEmpty()) {
            StringBuilder statusCounts = new StringBuilder();
            blockedCounts.forEach((status, count) -> {
                if (!statusCounts.isEmpty()) statusCounts.append(", ");
                statusCounts.append(count).append(" ").append(status.name());
            });
            throw new IllegalStateException(
                    String.format("Cannot update location '%s' as it is used by %s event session(s) with blocked statuses (%s). " + "This would affect attendance record integrity. Cancel or reassign those events first.", location.getLocationName(), statusCounts, statusCounts)
            );
        }

        if (request.getLocationName() != null) {
            String newName = request.getLocationName().trim();
            if (newName.isEmpty()) {
                throw new IllegalArgumentException("Location name cannot be blank");
            }
            if (!newName.equals(location.getLocationName())) {
                Optional<EventLocations> existing = locationRepository.findByLocationName(newName);
                if (existing.isPresent()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location with name '" + newName + "' already exists");
                }
            }
            location.setLocationName(newName);
        }
        if (request.getLocationType() != null) {
            if (request.getLocationType().trim().isEmpty()) {
                throw new IllegalArgumentException("Location type cannot be blank");
            }
            location.setLocationType(request.getLocationType().trim());
        }
        if (request.getGeoJsonData() != null) {
            try {
                GeoJsonPolygon polygon = convertToGeoJsonPolygon(request.getGeoJsonData());
                location.setGeometry(polygon);
                location.setGeometryType("Polygon");
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid geometry data: " + e.getMessage());
            }
        }
        location.setUpdatedAt(LocalDateTime.now());
        location = locationRepository.save(location);
        return convertToResponseDTO(location);
    }

    /**
     * Retrieves all event locations.
     *
     * @return a list of {@link LocationResponse} DTOs for all locations, including computed centroids
     */
    public List<LocationResponse> getAllLocations() {
        List<EventLocations> locations = locationRepository.findAll();
        return locations.stream().map(this::convertToResponseDTO).toList();
    }

    /**
     * Deletes an event location by its unique identifier.
     *
     * @param locationId the unique ID of the location to delete
     */
    public void deleteLocationById(String locationId) {
        boolean exists = locationRepository.existsById(locationId);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The location that you are trying to delete has not been found: " + locationId);
        }

        EventLocations location = locationRepository.findById(locationId).orElseThrow(() -> new IllegalStateException("Location not found after existence check: " + locationId));

        Long eventSessionDependentCount = eventSessionsRepository.countByEventLocationId(locationId);
        Long attendanceRecordsDependentCount = attendanceRecordsRepository.countByEventLocationId(locationId);

        if (eventSessionDependentCount > 0 || attendanceRecordsDependentCount > 0) {
            StringBuilder message = new StringBuilder("Cannot delete location '").append(location.getLocationName()).append("' as it is used by ");
            boolean hasEvents = eventSessionDependentCount > 0;
            boolean hasAttendance = attendanceRecordsDependentCount > 0;
            if (hasEvents && hasAttendance) {
                message.append(eventSessionDependentCount).append(" event session(s) and ").append(attendanceRecordsDependentCount).append(" attendance record(s)");
            } else if (hasEvents) {
                message.append(eventSessionDependentCount).append(" event session(s)");
            } else {
                message.append(attendanceRecordsDependentCount).append(" attendance record(s)");
            }
            message.append(". Please reassign or delete the dependent records first.");
            throw new IllegalStateException(message.toString());
        }
        locationRepository.deleteById(locationId);
        log.info("Deleted location: {}", locationId);
    }

    /**
     * PRIVATE HELPERS
     */

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
