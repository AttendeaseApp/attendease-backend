package com.attendease.backend.exceptions.domain.Event;

public class EventLocationConflictException extends RuntimeException {
	public EventLocationConflictException(String message) {
		super(message);
	}
}
