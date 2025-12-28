package com.attendease.backend.domain.academic.update;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class UpdateAcademicYearRequest {

	@NotBlank(message = "Academic year name is required")
	private String academicYearName;

	@NotNull(message = "First semester start date is required")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate firstSemesterStart;

	@NotNull(message = "First semester end date is required")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate firstSemesterEnd;

	@NotNull(message = "Second semester start date is required")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate secondSemesterStart;

	@NotNull(message = "Second semester end date is required")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate secondSemesterEnd;
}
