package com.attendease.backend.exceptions.domain.Location;

/**
 * Exception thrown when a location environment is invalid for the requested operation.
 * For example is when trying to enable GPS-based attendance monitoring for INDOOR venues.
 */
public class InvalidLocationEnvironmentException extends RuntimeException {
	public InvalidLocationEnvironmentException(String message) {
		super(message);
	}
}
