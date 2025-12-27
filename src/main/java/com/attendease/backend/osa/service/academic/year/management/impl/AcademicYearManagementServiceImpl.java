package com.attendease.backend.osa.service.academic.year.management.impl;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.academic.info.AcademicYearResponse;
import com.attendease.backend.domain.enums.academic.Semester;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import com.attendease.backend.repository.academic.AcademicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
	public AcademicYearResponse createAcademicYear(Academic academicYear) {
		validateAcademicYearDates(academicYear);
		validateNoDateOverlapWithExistingYears(academicYear, null);
		if (academicRepository.findByAcademicYearName(academicYear.getAcademicYearName()).isPresent()) {
			throw new IllegalArgumentException(
					"Academic year with name '" + academicYear.getAcademicYearName() + "' already exists"
			);
		}
		academicYear.setCurrentSemester(calculateCurrentSemester(academicYear));
		if (academicYear.isActive()) {
			validateCanActivateAcademicYear(academicYear);
			deactivateAllAcademicYears();
		}
		Academic savedAcademic = academicRepository.save(academicYear);
		return AcademicYearResponse.fromEntity(savedAcademic);
	}

	@Override
	public List<AcademicYearResponse> getAllAcademicYears() {
		return academicRepository.findAll().stream()
				.map(AcademicYearResponse::fromEntity)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<AcademicYearResponse> getActiveAcademicYear() {
		return academicRepository.findByIsActive(true).map(AcademicYearResponse::fromEntity);
	}

	@Override
	public AcademicYearResponse getAcademicYearById(String id) {
		Academic academic = academicRepository.findById(id).orElseThrow(() -> new RuntimeException("Academic year not found"));
		return AcademicYearResponse.fromEntity(academic);
	}

	@Override
	@Transactional
	public AcademicYearResponse updateAcademicYear(String id, Academic academicYear) {
		Academic existing = academicRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Academic year not found"));
		validateAcademicYearDates(academicYear);
		validateNoDateOverlapWithExistingYears(academicYear, id);

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
			validateCanActivateAcademicYear(existing);
			deactivateAllAcademicYears();
			existing.setActive(true);
		} else if (!academicYear.isActive() && existing.isActive()) {
			existing.setActive(false);
		}

		Academic savedAcademic = academicRepository.save(existing);
		return AcademicYearResponse.fromEntity(savedAcademic);
	}

	// TODO: Check for dependencies (events, attendance records, sections, etc.)
	@Override
	@Transactional
	public void deleteAcademicYear(String id) {
		Academic academicYear = academicRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Academic year not found"));
		if (academicYear.isActive()) {
			throw new IllegalStateException(
					"Cannot delete an active academic year. Deactivate it first."
			);
		}
		academicRepository.deleteById(id);
	}

	@Override
	@Transactional
	public AcademicYearResponse setActiveAcademicYear(String id) {
		Academic academicYear = academicRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Academic year not found"));

		validateCanActivateAcademicYear(academicYear);
		deactivateAllAcademicYears();
		academicYear.setActive(true);
		academicYear.setCurrentSemester(calculateCurrentSemester(academicYear));
		Academic savedAcademic = academicRepository.save(academicYear);
		return AcademicYearResponse.fromEntity(savedAcademic);
	}

	@Override
	public String getCurrentSemesterName() {
		return academicRepository.findByIsActive(true)
				.map(Academic::getCurrentSemester)
				.map(Semester::getDisplayName)
				.orElse(null);
	}

	@Override
	public Integer getCurrentSemester() {
		return academicRepository.findByIsActive(true)
				.map(Academic::getCurrentSemester)
				.map(Semester::getNumber)
				.orElse(null);
	}

	@Override
	public boolean isFirstSemesterActive() {
		return academicRepository.findByIsActive(true)
				.map(Academic::getCurrentSemester)
				.map(s -> s == Semester.FIRST)
				.orElse(false);
	}

	@Override
	public boolean isSecondSemesterActive() {
		return academicRepository.findByIsActive(true)
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
			throw new IllegalArgumentException(
					"First semester start date must be before end date"
			);
		}
		if (secondSemStart.isAfter(secondSemEnd)) {
			throw new IllegalArgumentException(
					"Second semester start date must be before end date"
			);
		}
		if (!firstSemEnd.isBefore(secondSemStart)) {
			throw new IllegalArgumentException(
					"First semester must end before second semester starts. There should be at least one day gap."
			);
		}
		// ~8 months
		long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(firstSemStart, secondSemEnd);
		if (daysBetween < 240) {
			throw new IllegalArgumentException(
					"Academic year is too short. It should span at least 8 months."
			);
		}
		// ~13 months
		if (daysBetween > 400) {
			throw new IllegalArgumentException(
					"Academic year is too long. It should not exceed 13 months."
			);
		}
	}

	private void validateNoDateOverlapWithExistingYears(Academic academicYear, String excludeId) {
		List<Academic> allYears = academicRepository.findAll();
		LocalDate newStart = academicYear.getFirstSemesterStart();
		LocalDate newEnd = academicYear.getSecondSemesterEnd();
		for (Academic existing : allYears) {
			if (existing.getId().equals(excludeId)) {
				continue;
			}
			LocalDate existingStart = existing.getFirstSemesterStart();
			LocalDate existingEnd = existing.getSecondSemesterEnd();
			if (datesOverlap(newStart, newEnd, existingStart, existingEnd)) {
				throw new IllegalArgumentException(
						"Academic year dates overlap with existing academic year '" +
								existing.getAcademicYearName() + "' (" + existingStart + " to " + existingEnd + ")"
				);
			}
		}
	}

	private void validateCanActivateAcademicYear(Academic academicYear) {
		LocalDate today = LocalDate.now();
		LocalDate yearStart = academicYear.getFirstSemesterStart();
		LocalDate yearEnd = academicYear.getSecondSemesterEnd();

		if (today.isAfter(yearEnd)) {
			throw new IllegalStateException(
					"Cannot activate academic year '" + academicYear.getAcademicYearName() +
							"' because it has already ended on " + yearEnd
			);
		}
		if (today.isBefore(yearStart.minusDays(30))) {
			throw new IllegalStateException(
					"Cannot activate academic year '" + academicYear.getAcademicYearName() +
							"' because it doesn't start until " + yearStart +
							". You can only activate it within 30 days of its start date."
			);
		}
		Optional<Academic> currentActive = academicRepository.findByIsActive(true);
		if (currentActive.isPresent() && !currentActive.get().getId().equals(academicYear.getId())) {
			Academic active = currentActive.get();
			if (today.isBefore(active.getSecondSemesterEnd()) || today.isEqual(active.getSecondSemesterEnd())) {
				throw new IllegalStateException(
						"Cannot activate academic year. Academic year '" + active.getAcademicYearName() +
								"' is still in progress (ends on " + active.getSecondSemesterEnd() + ")"
				);
			}
		}
	}

	private void deactivateAllAcademicYears() {
		List<Academic> activeYears = academicRepository.findAll().stream()
				.filter(Academic::isActive)
				.toList();
		if (!activeYears.isEmpty()) {
			activeYears.forEach(year -> year.setActive(false));
			academicRepository.saveAll(activeYears);
		}
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

	private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
		return !start1.isAfter(end2) && !start2.isAfter(end1);
	}
}