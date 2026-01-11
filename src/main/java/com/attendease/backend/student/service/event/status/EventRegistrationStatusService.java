package com.attendease.backend.student.service.event.status;

import com.attendease.backend.domain.event.registration.EventRegistrationStatusResponse;

/**
 * Service for checking student event registration status.
 * Used to determine if a student is already registered for an event.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2026-Jan-07
 */
public interface EventRegistrationStatusService {

	/**
	 * Checks if the authenticated student is already registered for the given event.
	 *
	 * @param authenticatedUserId The authenticated user ID
	 * @param eventId The event ID to check registration for
	 * @return Registration status response with detailed information
	 */
	EventRegistrationStatusResponse checkRegistrationStatus(String authenticatedUserId, String eventId);
}
