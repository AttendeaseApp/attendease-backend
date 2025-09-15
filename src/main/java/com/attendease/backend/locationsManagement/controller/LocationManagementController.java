package com.attendease.backend.locationsManagement.controller;

import com.attendease.backend.locationsManagement.service.LocationsManagementService;
import com.attendease.backend.model.locations.Request.EventLocationRequest;
import com.attendease.backend.model.locations.Response.LocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class LocationManagementController {

    private final LocationsManagementService locationsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse createLocation(@RequestBody EventLocationRequest request) {
        return locationsService.createLocation(request);
    }

    @GetMapping
    public List<LocationResponse> getAllLocations() throws Exception {
        return locationsService.getAllLocations();
    }

    @DeleteMapping("/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable String locationId) throws Exception {
        locationsService.deleteLocationById(locationId);
    }
}

