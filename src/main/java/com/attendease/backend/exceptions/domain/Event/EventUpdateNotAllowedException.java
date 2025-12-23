package com.attendease.backend.exceptions.domain.Event;

public class EventUpdateNotAllowedException extends RuntimeException {
	public EventUpdateNotAllowedException(String message) {
		super(message);
	}
}
