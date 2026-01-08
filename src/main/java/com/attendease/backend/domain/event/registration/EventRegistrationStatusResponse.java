package com.attendease.backend.domain.event.registration;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO containing student's registration status for an event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationStatusResponse {

	private boolean isRegistered;

	private AttendanceStatus attendanceStatus;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime registrationTime;

	private String attendanceRecordId;
	private String registrationLocationName;
	private String message;
	private String academicYearName;
	private Integer semester;
}
