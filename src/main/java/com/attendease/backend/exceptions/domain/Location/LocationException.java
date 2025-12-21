package com.attendease.backend.exceptions.domain.Location;

public class LocationException extends RuntimeException {

    protected LocationException(String message) {
        super(message);
    }

    protected LocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
