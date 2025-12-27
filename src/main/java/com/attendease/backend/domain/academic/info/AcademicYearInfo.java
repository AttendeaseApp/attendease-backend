package com.attendease.backend.domain.academic.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Academic year information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class AcademicYearInfo {
	private String id;
	private String academicYearName;
	private String currentSemester;
	private Integer currentSemesterNumber;
	private boolean isActive;
}
