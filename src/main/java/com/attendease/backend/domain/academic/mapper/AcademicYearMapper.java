package com.attendease.backend.domain.academic.mapper;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.academic.update.UpdateAcademicYearRequest;

/**
 * Mapper class for converting between Academic entity and request DTOs
 * for more reusability and cleaner service ipl classes.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-28
 */
public final class AcademicYearMapper {
	public static Academic toEntity(UpdateAcademicYearRequest request) {
		return Academic.builder()
				.academicYearName(request.getAcademicYearName())
				.firstSemesterStart(request.getFirstSemesterStart())
				.firstSemesterEnd(request.getFirstSemesterEnd())
				.secondSemesterStart(request.getSecondSemesterStart())
				.secondSemesterEnd(request.getSecondSemesterEnd())
				.build();
	}
}
