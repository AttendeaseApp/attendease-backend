package com.attendease.backend.exceptions.domain.Biometrics;

public class FacialRecognitionServiceException extends RuntimeException {
	public FacialRecognitionServiceException(String message) {
		super(message);
	}

	public FacialRecognitionServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
