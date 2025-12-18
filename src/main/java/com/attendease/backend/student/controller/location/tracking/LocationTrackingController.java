package com.attendease.backend.student.controller.location.tracking;

import com.attendease.backend.domain.locations.Request.LocationTrackingRequest;
import com.attendease.backend.domain.locations.Response.LocationTrackingResponse;
import com.attendease.backend.student.service.location.tracking.LocationTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LocationTrackingController {

    private final LocationTrackingService locationTrackingService;

    /**
     * Receives the student's geolocation via WebSocket and returns whether
     * they are inside or outside the event boundary.
     * Each user receives only their own result (not broadcasted).
     */
    @MessageMapping("/observe-current-location")
    @SendToUser("/queue/location-tracking")
    public LocationTrackingResponse trackCurrentLocation(@Payload LocationTrackingRequest request) {
        return locationTrackingService.trackCurrentLocation(request);
    }
}
