package com.attendease.backend.domain.student.history;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SectionHistory {

	private String sectionId;
	private String sectionName;
	private String academicYearId;
	private String academicYearName;
	private String yearLevel;
	private LocalDate startDate;
	private LocalDate endDate;
}
