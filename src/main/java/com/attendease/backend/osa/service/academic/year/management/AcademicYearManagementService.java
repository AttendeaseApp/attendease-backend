package com.attendease.backend.osa.service.academic.year.management;

import com.attendease.backend.domain.academic.Academic;

import java.util.List;
import java.util.Optional;

public interface AcademicYearManagementService {

	Academic createAcademicYear(Academic academicYear);

	List<Academic> getAllAcademicYears();

	Optional<Academic> getActiveAcademicYear();

	Academic getAcademicYearById(String id);

	Academic updateAcademicYear(String id, Academic academicYear);

	void deleteAcademicYear(String id);

	Academic setActiveAcademicYear(String id);

	Integer getCurrentSemester();

	String getCurrentSemesterName();

	boolean isFirstSemesterActive();

	boolean isSecondSemesterActive();
}
