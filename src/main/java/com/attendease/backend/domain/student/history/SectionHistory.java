package com.attendease.backend.domain.student.history;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SectionHistory {

	private String sectionId;
	private String sectionName;
	private String academicYearId;
	private LocalDate startDate;
	private LocalDate endDate;
}
