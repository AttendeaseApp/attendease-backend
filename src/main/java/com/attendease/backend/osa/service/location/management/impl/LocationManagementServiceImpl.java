package com.attendease.backend.osa.service.location.management.impl;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.enums.location.LocationEnvironment;
import com.attendease.backend.domain.enums.location.LocationPurpose;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.location.Location;
import com.attendease.backend.domain.location.geometry.LocationGeometry;
import com.attendease.backend.domain.location.management.LocationManagementRequest;
import com.attendease.backend.domain.location.management.LocationManagementResponse;
import com.attendease.backend.exceptions.domain.Location.*;
import com.attendease.backend.osa.service.location.management.LocationManagementService;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.location.LocationRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
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
import org.springframework.stereotype.Service;

/**
 * Implementation of location management service.
 *
 * <p>This class is marked as {@code final} to prevent inheritance and ensure
 * the implementation remains as designed. Service behavior should be modified
 * through configuration or by implementing a different service class, not by
 * extending this one.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-16 refactored in 2025-Dec-21, happy holidays!
 */
@Slf4j
@Service
@RequiredArgsConstructor
public final class LocationManagementServiceImpl implements LocationManagementService {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    @Override
    public LocationManagementResponse createNewLocation(LocationManagementRequest request) {
        String trimmedName = request.getLocationName().trim();

        LocationEnvironment environment = parseEnvironment(request.getLocationType());
        LocationPurpose purpose = parsePurpose(request.getLocationPurpose());

        Optional<Location> existing = locationRepository.findByLocationNameAndEnvironmentAndPurpose(trimmedName, environment, purpose);
        if (existing.isPresent()) {
            throw new LocationAlreadyExistsException(trimmedName, environment, purpose);
        }

        GeoJsonPolygon polygon = toGeoJsonPolygon(request.getLocationGeometry());

        Location location = Location.builder()
                .locationName(trimmedName)
                .environment(environment)
                .purpose(purpose)
                .description(request.getDescription())
                .locationGeometry(polygon)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        location = locationRepository.save(location);
        log.info("Created new location: {} with ID: {}", location.getLocationName(), location.getLocationId());

        return toResponseDTO(location);
    }

    @Override
    public LocationManagementResponse updateLocation(String locationId, LocationManagementRequest request) {
        Location location = locationRepository.findById(locationId).orElseThrow(() -> new LocationNotFoundException(locationId));

        checkForActiveEvents(locationId, location.getLocationName());

        if (request.getLocationName() != null) {
            String newLocationName = request.getLocationName().trim();
            LocationEnvironment newEnvironment = parseEnvironment(request.getLocationType());
            LocationPurpose newPurpose = parsePurpose(request.getLocationPurpose());

            if (!newLocationName.equals(location.getLocationName()) ||
                    !newEnvironment.equals(location.getEnvironment()) ||
                    !newPurpose.equals(location.getPurpose())) {

                Optional<Location> existing = locationRepository.findByLocationNameAndEnvironmentAndPurpose(newLocationName, newEnvironment, newPurpose);
                if (existing.isPresent() && !existing.get().getLocationId().equals(locationId)) {
                    throw new LocationAlreadyExistsException(newLocationName, newEnvironment, newPurpose);
                }

                location.setLocationName(newLocationName);
                location.setEnvironment(newEnvironment);
                location.setPurpose(newPurpose);
            }
        }

        if (request.getLocationGeometry() != null) {
            GeoJsonPolygon polygon = toGeoJsonPolygon(request.getLocationGeometry());
            location.setLocationGeometry(polygon);
        }

        if (request.getDescription() != null) {
            location.setDescription(request.getDescription().trim());
        }

        location.setUpdatedAt(LocalDateTime.now());
        location = locationRepository.save(location);
        log.info("Updated location: {}", locationId);

        return toResponseDTO(location);
    }

    @Override
    public List<LocationManagementResponse> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream().map(this::toResponseDTO).toList();
    }

    @Override
    public void deleteLocationById(String locationId) {
        Location location = locationRepository.findById(locationId).orElseThrow(() -> new LocationNotFoundException(locationId));

        Long venueEventCount = eventRepository.countByVenueLocationId(locationId);
        Long regEventCount = eventRepository.countByRegistrationLocationId(locationId);
        Long attendanceCountByEventLocationId = attendanceRecordsRepository.countByEventLocationId(locationId);
        Long attendanceCountByLocationRef = attendanceRecordsRepository.countByLocationLocationId(locationId);

        if (venueEventCount > 0 || regEventCount > 0 || attendanceCountByEventLocationId > 0 || attendanceCountByLocationRef > 0) {
            throw new LocationInUseException(
                    location.getLocationName(),
                    venueEventCount + regEventCount,
                    attendanceCountByEventLocationId + attendanceCountByLocationRef,
                    "delete"
            );
        }

        locationRepository.deleteById(locationId);
        log.info("Deleted location: {} ({})", location.getLocationName(), locationId);
    }

    /*
     * PRIVATE HELPERS
     */

    private LocationEnvironment parseEnvironment(String type) {
        String normalized = type.trim().toUpperCase();
        try {
            return LocationEnvironment.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(LocationEnvironment.values()).map(Enum::name).collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    String.format("Invalid location type '%s'. Valid values are: %s", type, validValues)
            );
        }
    }

    private LocationPurpose parsePurpose(String purpose) {
        String normalized = purpose.trim().toUpperCase();
        try {
            return LocationPurpose.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(LocationPurpose.values()).map(Enum::name).collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    String.format("Invalid location purpose '%s'. Valid values are: %s", purpose, validValues)
            );
        }
    }

    private void checkForActiveEvents(String locationId, String locationName) {
        List<Event> referencingEvents = eventRepository.findByVenueLocationId(locationId);
        Set<EventStatus> blockedStatuses = Set.of(
                EventStatus.REGISTRATION,
                EventStatus.ONGOING,
                EventStatus.CONCLUDED,
                EventStatus.FINALIZED
        );

        Map<EventStatus, Long> blockedCounts = referencingEvents.stream().filter(event -> blockedStatuses.contains(event.getEventStatus()))
                .collect(Collectors.groupingBy(Event::getEventStatus, Collectors.counting()));

        if (!blockedCounts.isEmpty()) {
            StringBuilder statusCounts = new StringBuilder();
            blockedCounts.forEach((status, count) -> {
                if (!statusCounts.isEmpty()) statusCounts.append(", ");
                statusCounts.append(count).append(" ").append(status.name());
            });
            throw new LocationHasActiveEventsException(locationName, statusCounts.toString());
        }
    }

    private GeoJsonPolygon toGeoJsonPolygon(LocationGeometry locationGeometry) {
        if (locationGeometry == null || !"Polygon".equalsIgnoreCase(locationGeometry.getType())) {
            throw new InvalidGeometryException("Only Polygon geometry is supported");
        }

        if (locationGeometry.getCoordinates() == null || locationGeometry.getCoordinates().isEmpty()) {
            throw new InvalidGeometryException("Polygon coordinates are required");
        }

        List<List<Double>> outerRing = locationGeometry.getCoordinates().getFirst();

        if (outerRing == null || outerRing.size() < 4) {
            throw new InvalidGeometryException("Polygon must have at least 4 points (minimum 3 unique points + closing point)");
        }

        List<Point> points = outerRing.stream()
                .map(coord -> {
                    if (coord == null || coord.size() < 2) {
                        throw new InvalidGeometryException("Each coordinate must have [longitude, latitude]");
                    }
                    return new Point(coord.get(0), coord.get(1));
                }).toList();

        Point first = points.getFirst();
        Point last = points.getLast();

        if (!first.equals(last)) {
            throw new InvalidGeometryException("Polygon must be closed: first point must equal last point");
        }

        return new GeoJsonPolygon(points);
    }

    private LocationManagementResponse toResponseDTO(Location location) {
        if (location == null) {
            throw new IllegalStateException("Location data is null");
        }

        LocationManagementResponse response = new LocationManagementResponse();
        response.setLocationId(location.getLocationId());
        response.setLocationName(location.getLocationName());
        response.setDescription(location.getDescription());
        response.setLocationEnvironment(location.getEnvironment());
        response.setLocationPurposeType(location.getPurpose());
        response.setCreatedAt(location.getCreatedAt());
        response.setUpdatedAt(location.getUpdatedAt());

        GeoJsonPolygon polygon = location.getLocationGeometry();
        if (polygon != null) {
            response.setGeometryType(polygon.getType());
            calculateAndSetCentroid(polygon, response);
        }

        return response;
    }

    private void calculateAndSetCentroid(GeoJsonPolygon polygon, LocationManagementResponse response) {
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
                        sumLng += point.getX();
                        sumLat += point.getY();
                    }

                    response.setLatitude(sumLat / count);
                    response.setLongitude(sumLng / count);
                }
            }
        }
    }
}