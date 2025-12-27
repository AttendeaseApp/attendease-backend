package com.attendease.backend.domain.academic.semester;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Semester information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class SemesterInfo {

	private Integer number;
	private String name;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	private Long durationDays;
	private boolean isActive;
}
