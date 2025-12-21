package com.attendease.backend.exceptions.domain.Location;

public class InvalidGeometryException extends LocationException {

    public InvalidGeometryException(String message) {
        super("Invalid geometry: " + message);
    }

    public InvalidGeometryException(String message, Throwable cause) {
        super("Invalid geometry: " + message, cause);
    }
}
