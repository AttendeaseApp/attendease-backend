package com.attendease.backend.eventLocationManagement.service;

import com.attendease.backend.eventLocationManagement.dto.*;
import com.attendease.backend.eventLocationManagement.dto.request.GeoJsonRequestDTO;
import com.attendease.backend.eventLocationManagement.dto.response.LocationResponseDTO;
import com.attendease.backend.eventLocationManagement.repository.LocationRepository;
import com.attendease.backend.model.locations.EventLocations;
import com.attendease.backend.model.locations.GeofenceData;
import com.google.cloud.firestore.GeoPoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LocationResponseDTO createLocationFromGeoJson(LocationCreateGeoJsonDTO createRequest)
            throws ExecutionException, InterruptedException {

        GeometryDTO geometry = extractGeometry(createRequest.getGeoJsonData());
        List<List<List<Double>>> polygonCoords = geometry.getCoordinates();

        if (polygonCoords == null || polygonCoords.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Polygon coordinates are missing");
        }

        EventLocations location = new EventLocations();
        location.setLocationName(createRequest.getLocationName());
        location.setLocationType(createRequest.getLocationType());
        location.setGeometryType("POLYGON");

        List<GeoPoint> flattenedCoordinates = new ArrayList<>();
        List<Integer> ringBreaks = new ArrayList<>();
        int currentIndex = 0;

        for (List<List<Double>> ring : polygonCoords) {
            if (currentIndex > 0) {
                ringBreaks.add(currentIndex);
            }

            for (List<Double> coord : ring) {
                if (coord.size() < 2) continue;

                double longitude = coord.get(0);
                double latitude = coord.get(1);

                if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid coordinates: [" + latitude + ", " + longitude + "]");
                }

                flattenedCoordinates.add(new GeoPoint(latitude, longitude));
                currentIndex++;
            }

            int ringSize = currentIndex - (ringBreaks.isEmpty() ? 0 : ringBreaks.getLast());
            if (ringSize < 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Polygon ring must have at least 3 points");
            }
        }

        location.setPolygonCoordinates(flattenedCoordinates);
        location.setRingBreaks(ringBreaks.isEmpty() ? null : ringBreaks);

        List<List<Double>> outerRing = polygonCoords.getFirst();
        PolygonCoordinateDTO centroid = calculateCentroidFromCoordinates(outerRing);
        location.setGeomPoints(new GeoPoint(centroid.getLatitude(), centroid.getLongitude()));

        if (createRequest.getGeofenceParameters() != null) {
            GeofenceParametersDTO params = createRequest.getGeofenceParameters();
            GeofenceData geofenceData = new GeofenceData(
                    params.getRadiusMeters(),
                    params.getAlertType(),
                    params.isActive()
            );
            location.setGeofenceData(geofenceData);
        }

        String locationId = locationRepository.save(location);
        location.setLocationId(locationId);

        return convertToResponseDTO(location);
    }


    public List<LocationResponseDTO> getAllLocations() throws ExecutionException, InterruptedException {
        List<EventLocations> locations = locationRepository.findAll();
        return locations.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public void deleteLocationById(String locationId) throws ExecutionException, InterruptedException {
        boolean exists = locationRepository.existsById(locationId);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found: " + locationId);
        }

        locationRepository.deleteById(locationId);
    }

    //PRIVATE HELPERS

    private GeometryDTO extractGeometry(GeoJsonRequestDTO geoJson) {
        if (geoJson == null || geoJson.getFeatures() == null || geoJson.getFeatures().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GeoJSON features are missing");
        }

        GeometryDTO geometry = geoJson.getFeatures().getFirst().getGeometry();
        if (geometry == null || !"Polygon".equalsIgnoreCase(geometry.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Polygon geometry is supported");
        }

        return geometry;
    }

    private PolygonCoordinateDTO calculateCentroidFromCoordinates(List<List<Double>> ring) {
        if (ring == null || ring.size() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Centroid calculation requires at least 3 points");
        }

        double sumLat = 0;
        double sumLng = 0;
        int count = ring.size();

        if (ring.getFirst().equals(ring.getLast())) {
            count--;
        }

        for (int i = 0; i < count; i++) {
            List<Double> coord = ring.get(i);
            sumLng += coord.get(0);
            sumLat += coord.get(1);
        }

        return new PolygonCoordinateDTO(sumLat / count, sumLng / count);
    }

    private LocationResponseDTO convertToResponseDTO(EventLocations location) {
        if (location == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Location data is null");
        }

        LocationResponseDTO response = new LocationResponseDTO();
        response.setLocationId(location.getLocationId());
        response.setLocationName(location.getLocationName());
        response.setLocationType(location.getLocationType());

        if (location.getGeomPoints() != null) {
            response.setLatitude(location.getGeomPoints().getLatitude());
            response.setLongitude(location.getGeomPoints().getLongitude());
        }

        if (location.getGeofenceData() != null) {
            GeofenceData data = location.getGeofenceData();
            assert location.getGeomPoints() != null;
            GeofenceParametersDTO params = new GeofenceParametersDTO(
                    data.getRadiusMeters(),
                    location.getGeomPoints().getLatitude(),
                    location.getGeomPoints().getLongitude(),
                    data.getAlertType(),
                    data.isActive()
            );
            response.setGeofenceParameters(params);
        }

        return response;
    }
}
