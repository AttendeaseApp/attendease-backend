package com.attendease.backend.exceptions.domain.Event;

public class EventNotFoundException extends RuntimeException {
	public EventNotFoundException(String message) {
		super(message);
	}

	public EventNotFoundException(String eventId, boolean useId) {
		super("Event not found with ID: " + eventId);
	}
}