package com.attendease.backend.exceptions.domain.Event;

public class InvalidDateRangeException extends RuntimeException {
	public InvalidDateRangeException(String message) {
		super(message);
	}
}
