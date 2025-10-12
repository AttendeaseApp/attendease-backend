package com.attendease.backend.osaModule.service.management.location;

import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.locations.Geofencing.Geometry;
import com.attendease.backend.domain.locations.Request.EventLocationRequest;
import com.attendease.backend.domain.locations.Response.LocationResponse;
import com.attendease.backend.repository.locations.LocationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class LocationsManagementService {

    private final LocationRepository locationRepository;

    public LocationResponse createLocation(EventLocationRequest request) {
        GeoJsonPolygon polygon = convertToGeoJsonPolygon(request.getGeoJsonData());

        EventLocations location = EventLocations.builder()
                .locationName(request.getLocationName())
                .locationType(request.getLocationType())
                .geometryType("Polygon")
                .geometry(polygon)
                .build();

        location = locationRepository.save(location);

        return convertToResponseDTO(location);
    }

    public GeoJsonPolygon convertToGeoJsonPolygon(Geometry geometry) {
        if (geometry == null || !"Polygon".equalsIgnoreCase(geometry.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Polygon geometry is supported");
        }

        List<List<Double>> outerRing = geometry.getCoordinates().getFirst();

        List<Point> points = outerRing.stream()
                .map(coord -> new Point(coord.getFirst(), coord.get(1))).toList();

        return new GeoJsonPolygon(points);
    }

    public List<LocationResponse> getAllLocations() throws ExecutionException, InterruptedException {
        List<EventLocations> locations = locationRepository.findAll();
        return locations.stream().map(this::convertToResponseDTO).toList();
    }

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