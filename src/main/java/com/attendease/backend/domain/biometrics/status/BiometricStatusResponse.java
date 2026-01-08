package com.attendease.backend.domain.biometrics.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiometricStatusResponse {

	private String status;
	private String message;
	private String registeredDate;
}
