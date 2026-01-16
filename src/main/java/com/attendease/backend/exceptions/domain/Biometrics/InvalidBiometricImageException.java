package com.attendease.backend.exceptions.domain.Biometrics;

public class InvalidBiometricImageException extends RuntimeException {
	public InvalidBiometricImageException(String message) {
		super(message);
	}
}
