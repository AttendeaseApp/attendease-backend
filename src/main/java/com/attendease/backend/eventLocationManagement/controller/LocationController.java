package com.attendease.backend.eventLocationManagement.controller;

import com.attendease.backend.eventLocationManagement.dto.*;
import com.attendease.backend.eventLocationManagement.dto.response.LocationResponseDTO;
import com.attendease.backend.eventLocationManagement.service.LocationServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("v1/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationServiceInterface locationService;

    public LocationController(LocationServiceInterface locationService) {
        this.locationService = locationService;
    }

    /**
     * Create new reusable location using GeoJson
     *
     * Sample endpoint:
     * POST /v1/api/locations/create-new-location
     *
     * Body (manually extracted using https://geojson.io/#map=18.02/14.149746/120.955424):
     * {
     *   "locationName": "Rogationist College Gym Geometry",
     *   "locationType": "INDOOR",
     *   "geoJsonData": {
     *     "type": "FeatureCollection",
     *     "features": [
     *       {
     *         "type": "Feature",
     *         "properties": {},
     *         "geometry": {
     *           "type": "Polygon",
     *           "coordinates": [
     *             [
     *               [120.95539068474227, 14.15007476704713],
     *               [120.9553352439969, 14.149647257755817],
     *               [120.95576820981637, 14.149583259290495],
     *               [120.95583685073905, 14.150008208767218],
     *               [120.95539068474227, 14.15007476704713]
     *             ]
     *           ]
     *         }
     *       }
     *     ]
     *   },
     *   "geofenceParameters": {
     *     "radiusMeters": 100.0,
     *     "centerLatitude": 14.149828,
     *     "centerLongitude": 120.955584,
     *     "alertType": "entry",
     *     "isActive": true
     *   }
     * }
     */
    @PostMapping("/create-new-location")
    public ResponseEntity<LocationResponseDTO> createLocationFromGeoJson(@RequestBody LocationCreateGeoJsonDTO createRequest)
            throws ExecutionException, InterruptedException {
        LocationResponseDTO response = locationService.createLocationFromGeoJson(createRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all event sessions.
     * Sample endpoint:
     * GET v1/api/locations/all
     */
    @GetMapping("all")
    public ResponseEntity<List<LocationResponseDTO>> getAllLocations() throws ExecutionException, InterruptedException {
        List<LocationResponseDTO> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * Delete an event session.
     * Sample endpoint:
     * DELETE v1/api/locations/{locationId}
     */
    @DeleteMapping("/{locationId}")
    public ResponseEntity<?> deleteLocation(@PathVariable String locationId) throws ExecutionException, InterruptedException {
        locationService.deleteLocationById(locationId);
        return ResponseEntity.noContent().build();
    }

}
