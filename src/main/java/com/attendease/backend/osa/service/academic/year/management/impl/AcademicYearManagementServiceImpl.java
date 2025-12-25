package com.attendease.backend.osa.service.academic.year.management.impl;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.enums.academic.Semester;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import com.attendease.backend.repository.academic.AcademicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing academic year.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-24
 */
@Service
@RequiredArgsConstructor
public final class AcademicYearManagementServiceImpl implements AcademicYearManagementService {

	private final AcademicRepository academicRepository;

	@Override
	@Transactional
	public Academic createAcademicYear(Academic academicYear) {
		validateAcademicYearDates(academicYear);
		if (academicRepository.findByAcademicYearName(academicYear.getAcademicYearName()).isPresent()) {
			throw new IllegalArgumentException("Academic year with name '" + academicYear.getAcademicYearName() + "' already exists");
		}
		academicYear.setCurrentSemester(calculateCurrentSemester(academicYear));
		if (academicYear.isActive()) {
			deactivateAllAcademicYears();
		}
		return academicRepository.save(academicYear);
	}

	@Override
	public List<Academic> getAllAcademicYears() {
		return academicRepository.findAll();
	}

	@Override
	public Optional<Academic> getActiveAcademicYear() {
		return academicRepository.findByIsActive(true);
	}

	@Override
	public Academic getAcademicYearById(String id) {
		return academicRepository.findById(id).orElseThrow(() -> new RuntimeException("Academic year not found"));
	}

	@Override
	@Transactional
	public Academic updateAcademicYear(String id, Academic academicYear) {
		// TODO: prevent updating of academic year with dependencies on EVENT, ATTENDANCE RECORDS, COURSE, SECTION, ETC.
		Academic existing = getAcademicYearById(id);
		validateAcademicYearDates(academicYear);

		academicRepository.findByAcademicYearName(academicYear.getAcademicYearName())
				.filter(a -> !a.getId().equals(id))
				.ifPresent(a -> {
					throw new IllegalArgumentException("Academic year name already exists");
				});

		existing.setAcademicYearName(academicYear.getAcademicYearName());
		existing.setFirstSemesterStart(academicYear.getFirstSemesterStart());
		existing.setFirstSemesterEnd(academicYear.getFirstSemesterEnd());
		existing.setSecondSemesterStart(academicYear.getSecondSemesterStart());
		existing.setSecondSemesterEnd(academicYear.getSecondSemesterEnd());
		existing.setCurrentSemester(calculateCurrentSemester(existing));

		if (academicYear.isActive() && !existing.isActive()) {
			deactivateAllAcademicYears();
			existing.setActive(true);
		}

		return academicRepository.save(existing);
	}

	@Override
	@Transactional
	public void deleteAcademicYear(String id) {
		Academic academicYear = getAcademicYearById(id);
		if (academicYear.isActive()) {
			// TODO: prevent deletion of academic year with dependencies on EVENT, ATTENDANCE RECORDS, COURSE, SECTION, ETC.
			throw new IllegalStateException("You cannot delete an active academic year.");
		}
		academicRepository.deleteById(id);
	}

	@Override
	@Transactional
	public Academic setActiveAcademicYear(String id) {
		// TODO: prevent activating academic year that is already finished
		Academic academicYear = getAcademicYearById(id);
		deactivateAllAcademicYears();
		academicYear.setActive(true);
		academicYear.setCurrentSemester(calculateCurrentSemester(academicYear));
		return academicRepository.save(academicYear);
	}

	@Override
	public String getCurrentSemesterName() {
		Optional<Academic> activeYear = getActiveAcademicYear();
		return activeYear
				.map(Academic::getCurrentSemester)
				.map(Semester::getDisplayName)
				.orElse(null);
	}

	@Override
	public Integer getCurrentSemester() {
		Optional<Academic> activeYear = getActiveAcademicYear();
		return activeYear
				.map(Academic::getCurrentSemester)
				.map(Semester::getNumber)
				.orElse(null);
	}

	@Override
	public boolean isFirstSemesterActive() {
		Optional<Academic> activeYear = getActiveAcademicYear();
		return activeYear
				.map(Academic::getCurrentSemester)
				.map(s -> s == Semester.FIRST)
				.orElse(false);
	}

	@Override
	public boolean isSecondSemesterActive() {
		Optional<Academic> activeYear = getActiveAcademicYear();
		return activeYear
				.map(Academic::getCurrentSemester)
				.map(s -> s == Semester.SECOND)
				.orElse(false);
	}

	/*
	 * PRIVATE HELPERS
	 */

	private void validateAcademicYearDates(Academic academicYear) {
		LocalDate firstSemStart = academicYear.getFirstSemesterStart();
		LocalDate firstSemEnd = academicYear.getFirstSemesterEnd();
		LocalDate secondSemStart = academicYear.getSecondSemesterStart();
		LocalDate secondSemEnd = academicYear.getSecondSemesterEnd();

		if (firstSemStart.isAfter(firstSemEnd)) {
			throw new IllegalArgumentException("First semester start date must be before end date");
		}
		if (secondSemStart.isAfter(secondSemEnd)) {
			throw new IllegalArgumentException("Second semester start date must be before end date");
		}
		if (firstSemEnd.isAfter(secondSemStart) || firstSemEnd.isEqual(secondSemStart)) {
			throw new IllegalArgumentException("First semester must end before second semester starts");
		}
	}

	@Transactional
	private void deactivateAllAcademicYears() {
		List<Academic> allYears = academicRepository.findAll();
		allYears.forEach(year -> {
			year.setActive(false);
			academicRepository.save(year);
		});
	}

	private Semester calculateCurrentSemester(Academic academic) {
		LocalDate today = LocalDate.now();

		if (isDateInRange(today, academic.getFirstSemesterStart(), academic.getFirstSemesterEnd())) {
			return Semester.FIRST;
		} else if (isDateInRange(today, academic.getSecondSemesterStart(), academic.getSecondSemesterEnd())) {
			return Semester.SECOND;
		}

		return null;
	}

	private boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
		return !date.isBefore(start) && !date.isAfter(end);
	}
}