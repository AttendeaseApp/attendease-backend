package com.attendease.backend.student.controller.event.registration;

import com.attendease.backend.domain.event.registration.EventRegistrationStatusResponse;
import com.attendease.backend.student.service.event.status.EventRegistrationStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for checking event registration status.
 * Provides endpoints for students to check if they're already registered.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2026-Jan-07
 */
@Slf4j
@RestController
@RequestMapping("/api/student/event/registration")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class EventRegistrationStatusController {

	private final EventRegistrationStatusService registrationStatusService;

	/**
	 * Check if the authenticated student is registered for a specific event.
	 * Used to show/hide registration buttons and display registration status.
	 *
	 * @param eventId The event ID to check
	 * @param authentication The authenticated user
	 * @return Registration status response
	 */
	@GetMapping("/status/{eventId}")
	public ResponseEntity<EventRegistrationStatusResponse> checkRegistrationStatus(@PathVariable String eventId, Authentication authentication) {
		String userId = authentication.getName();
		log.info("Checking registration status for user {} on event {}", userId, eventId);
		EventRegistrationStatusResponse response = registrationStatusService.checkRegistrationStatus(userId, eventId);
		return ResponseEntity.ok(response);
	}
}
