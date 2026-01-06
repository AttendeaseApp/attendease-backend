package com.attendease.backend.domain.location.tracking;

import lombok.Data;

/**
 * DTO for event-based location tracking requests.
 * Used when verifying location against event-specific boundaries.
 */
@Data
public class EventLocationTrackingRequest {

	private String eventId;
	private double latitude;
	private double longitude;
}
