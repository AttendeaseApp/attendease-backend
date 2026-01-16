package com.attendease.backend.exceptions.domain.Biometrics.Registration;

public class BiometricProcessingException extends RuntimeException {
	public BiometricProcessingException(String message) {
		super(message);
	}

	public BiometricProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
