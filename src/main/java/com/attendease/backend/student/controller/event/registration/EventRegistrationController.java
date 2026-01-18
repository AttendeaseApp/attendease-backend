package com.attendease.backend.student.controller.event.registration;

import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.event.registration.EventRegistrationRequest;
import com.attendease.backend.domain.exception.error.ErrorResponse;
import com.attendease.backend.student.service.location.tracking.LocationTrackingService;
import com.attendease.backend.student.service.event.registration.EventRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;
    private final LocationTrackingService locationTrackingService;
    private final ObjectMapper objectMapper;

    /**
     * Endpoint for student event registration.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerStudentToEvent(Authentication authentication, @RequestPart("registrationData") String registrationDataJson, @RequestPart(value = "faceImage", required = false) MultipartFile faceImage) {
        try {
            String authenticatedUserId = authentication.getName();
            EventRegistrationRequest registrationRequest = objectMapper.readValue(registrationDataJson, EventRegistrationRequest.class);
            if (registrationRequest.getEventId() == null || registrationRequest.getEventId().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("BAD_REQUEST", "Event ID is required", LocalDateTime.now()));
            }
            if (faceImage != null && !faceImage.isEmpty()) {
                String contentType = faceImage.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("INVALID_FILE_TYPE", "Invalid file type. Please upload an image file (JPEG, PNG, etc.)", LocalDateTime.now()));
                }
            }
            EventRegistrationRequest response = eventRegistrationService.eventRegistration(authenticatedUserId, registrationRequest, faceImage);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(), LocalDateTime.now()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("INVALID_JSON", "Invalid registration data format", LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("INTERNAL_ERROR", "Event registration failed. Please try again later.", LocalDateTime.now()));
        }
    }

    /**
     * Endpoint for sending periodic location pings from the client.
     * The authenticated user ID is automatically resolved from the security context.
     */
    @PostMapping("/ping")
    public ResponseEntity<?> venueLocationMonitoring(Authentication authentication, @RequestBody AttendanceTrackingResponse attendancePingLogs) {
        String authenticatedUserId = authentication.getName();
        boolean isInside = locationTrackingService.venueLocationMonitoring(authenticatedUserId, attendancePingLogs);
        return ResponseEntity.ok().body("Ping recorded successfully. Inside area: " + isInside);
    }


}
