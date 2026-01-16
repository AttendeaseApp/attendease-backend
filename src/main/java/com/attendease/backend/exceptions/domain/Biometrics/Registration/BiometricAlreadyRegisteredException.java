package com.attendease.backend.exceptions.domain.Biometrics.Registration;

public class BiometricAlreadyRegisteredException extends RuntimeException {
	public BiometricAlreadyRegisteredException(String message) {
		super(message);
	}
}
