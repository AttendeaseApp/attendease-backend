package com.attendease.backend.domain.academic.info;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.academic.semester.SemesterInfo;
import com.attendease.backend.domain.enums.academic.AcademicYearStatus;
import com.attendease.backend.domain.enums.academic.Semester;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Response DTO for Academic Year entity.
 * <p>
 * Provides comprehensive information about an academic year including calculated fields
 * such as status, days remaining, and progress information.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class AcademicYearResponse {

	private String id;
	private String academicYearName;
	private SemesterInfo currentSemester;
	private SemesterInfo firstSemester;
	private SemesterInfo secondSemester;
	private boolean isActive;
	private AcademicYearStatus status;
	private Long daysUntilStart;
	private Long daysUntilEnd;
	private Long totalDays;
	private Long daysElapsed;
	private Double progressPercentage;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	/**
	 * converts an academic entity to an response DTO with calculated fields
	 */
	public static AcademicYearResponse fromEntity(Academic academic) {
		if (academic == null) {
			return null;
		}

		LocalDate today = LocalDate.now();

		SemesterInfo firstSem = buildSemesterInfo(
				Semester.FIRST,
				academic.getFirstSemesterStart(),
				academic.getFirstSemesterEnd(),
				today
		);

		SemesterInfo secondSem = buildSemesterInfo(
				Semester.SECOND,
				academic.getSecondSemesterStart(),
				academic.getSecondSemesterEnd(),
				today
		);

		SemesterInfo currentSem = null;
		if (academic.getCurrentSemester() != null) {
			currentSem = academic.getCurrentSemester() == Semester.FIRST ? firstSem : secondSem;
		}

		AcademicYearStatus status = calculateStatus(academic, today);

		Long daysUntilStart = today.isBefore(academic.getFirstSemesterStart())
				? ChronoUnit.DAYS.between(today, academic.getFirstSemesterStart()) : null;

		Long daysUntilEnd = today.isBefore(academic.getSecondSemesterEnd())
				? ChronoUnit.DAYS.between(today, academic.getSecondSemesterEnd()) : null;

		Long totalDays = ChronoUnit.DAYS.between(
				academic.getFirstSemesterStart(),
				academic.getSecondSemesterEnd()
		);

		Long daysElapsed = null;
		Double progressPercentage = null;

		if (!today.isBefore(academic.getFirstSemesterStart()) && !today.isAfter(academic.getSecondSemesterEnd())) {
			daysElapsed = ChronoUnit.DAYS.between(academic.getFirstSemesterStart(), today);
			progressPercentage = (daysElapsed.doubleValue() / totalDays.doubleValue()) * 100.0;
		}

		return AcademicYearResponse.builder()
				.id(academic.getId())
				.academicYearName(academic.getAcademicYearName())
				.currentSemester(currentSem)
				.firstSemester(firstSem)
				.secondSemester(secondSem)
				.isActive(academic.isActive())
				.status(status)
				.daysUntilStart(daysUntilStart)
				.daysUntilEnd(daysUntilEnd)
				.totalDays(totalDays)
				.daysElapsed(daysElapsed)
				.progressPercentage(progressPercentage != null ? Math.round(progressPercentage * 100.0) / 100.0 : null)
				.createdAt(academic.getCreatedAt())
				.updatedAt(academic.getUpdatedAt())
				.build();
	}

	private static SemesterInfo buildSemesterInfo(Semester semester, LocalDate start, LocalDate end, LocalDate today) {
		boolean isActive = !today.isBefore(start) && !today.isAfter(end);
		Long duration = ChronoUnit.DAYS.between(start, end);
		return SemesterInfo.builder()
				.number(semester.getNumber())
				.name(semester.getDisplayName())
				.startDate(start)
				.endDate(end)
				.durationDays(duration)
				.isActive(isActive)
				.build();
	}
	private static AcademicYearStatus calculateStatus(Academic academic, LocalDate today) {
		LocalDate firstStart = academic.getFirstSemesterStart();
		LocalDate firstEnd = academic.getFirstSemesterEnd();
		LocalDate secondStart = academic.getSecondSemesterStart();
		LocalDate secondEnd = academic.getSecondSemesterEnd();
		if (today.isBefore(firstStart)) {
			return AcademicYearStatus.UPCOMING;
		} else if (today.isAfter(secondEnd)) {
			return AcademicYearStatus.COMPLETED;
		} else if (today.isAfter(firstEnd) && today.isBefore(secondStart)) {
			return AcademicYearStatus.BETWEEN_SEMESTERS;
		} else {
			return AcademicYearStatus.IN_PROGRESS;
		}
	}
}
