package com.attendease.backend.osa.service.academic.year.management.impl;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.academic.info.AcademicYearResponse;
import com.attendease.backend.domain.enums.academic.AcademicYearStatus;
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

		validateUpdateDoesNotChangeStatus(existing, academicYear);
		validateDateUpdatePermissions(existing, academicYear);

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
			validateCanDeactivateAcademicYear(existing);
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

		LocalDate today = LocalDate.now();
		AcademicYearStatus status = calculateStatus(academicYear, today);
		if (status == AcademicYearStatus.IN_PROGRESS ||
				status == AcademicYearStatus.BETWEEN_SEMESTERS) {
			throw new IllegalStateException(
					"Cannot delete an ongoing academic year (status: " + status + "). " +
							"Wait until after " + academicYear.getSecondSemesterEnd()
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
	@Transactional
	public AcademicYearResponse deactivateAcademicYear(String id) {
		Academic academicYear = academicRepository.findById(id).orElseThrow(() -> new RuntimeException("Academic year not found"));
		if (!academicYear.isActive()) {
			throw new IllegalStateException(
					"Academic year '" + academicYear.getAcademicYearName() + "' is already inactive"
			);
		}
		validateCanDeactivateAcademicYear(academicYear);
		academicYear.setActive(false);
		academicYear.setCurrentSemester(null);
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
		long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(firstSemStart, secondSemEnd);
		if (daysBetween < 240) {
			throw new IllegalArgumentException(
					"Academic year is too short. It should span at least 8 months (~240 days)."
			);
		}

		if (daysBetween > 400) {
			throw new IllegalArgumentException(
					"Academic year is too long. It should not exceed 13 months (~400 days)."
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
								existing.getAcademicYearName() + "' (" +
								existingStart + " to " + existingEnd + ")"
				);
			}
		}
	}

	private void validateUpdateDoesNotChangeStatus(Academic existing, Academic updated) {
		LocalDate today = LocalDate.now();
		AcademicYearStatus currentStatus = calculateStatus(existing, today);
		AcademicYearStatus newStatus = calculateStatus(updated, today);
		if (currentStatus != newStatus) {
			throw new IllegalStateException(
					"Cannot update academic year: changes would alter its status from " +
							currentStatus + " to " + newStatus + ". " +
							"Status changes must occur naturally through date progression."
			);
		}
	}

	private void validateDateUpdatePermissions(Academic existing, Academic updated) {
		LocalDate today = LocalDate.now();
		AcademicYearStatus status = calculateStatus(existing, today);

		switch (status) {
			case COMPLETED:
				if (!datesMatch(existing, updated)) {
					throw new IllegalStateException(
							"Cannot modify dates of academic year '" + existing.getAcademicYearName() +
									"' because it has already ended on " + existing.getSecondSemesterEnd()
					);
				}
				break;

			case IN_PROGRESS:
				Semester currentSem = existing.getCurrentSemester();
				if (currentSem == Semester.FIRST) {
					if (!updated.getFirstSemesterStart().equals(existing.getFirstSemesterStart()) ||
							!updated.getFirstSemesterEnd().equals(existing.getFirstSemesterEnd())) {
						throw new IllegalStateException(
								"Cannot modify first semester dates while it is currently in progress. " +
										"Only second semester dates can be updated."
						);
					}
					validateSemesterGap(updated.getFirstSemesterEnd(), updated.getSecondSemesterStart());

				} else if (currentSem == Semester.SECOND) {
					if (!datesMatch(existing, updated)) {
						throw new IllegalStateException(
								"Cannot modify any semester dates while second semester is in progress. " +
										"The academic year is too far along to allow changes."
						);
					}
				}
				break;

			case BETWEEN_SEMESTERS:
				if (!updated.getFirstSemesterStart().equals(existing.getFirstSemesterStart()) ||
						!updated.getFirstSemesterEnd().equals(existing.getFirstSemesterEnd())) {
					throw new IllegalStateException(
							"Cannot modify first semester dates during the break period. " +
									"First semester has already concluded. Only second semester dates can be updated."
					);
				}

				if (updated.getSecondSemesterStart().isBefore(today)) {
					throw new IllegalStateException(
							"Cannot move second semester start date to the past (" +
									updated.getSecondSemesterStart() + "). It must be today or in the future."
					);
				}
				break;

			case UPCOMING:
				if (updated.getFirstSemesterStart().isBefore(today.minusDays(30))) {
					throw new IllegalStateException(
							"Cannot set first semester start date more than 30 days in the past"
					);
				}
				break;
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
			if (today.isBefore(active.getSecondSemesterEnd()) ||
					today.isEqual(active.getSecondSemesterEnd())) {
				throw new IllegalStateException(
						"Cannot activate academic year. Academic year '" + active.getAcademicYearName() +
								"' is still in progress (ends on " + active.getSecondSemesterEnd() + ")"
				);
			}
		}
	}
	private void validateCanDeactivateAcademicYear(Academic academicYear) {
		LocalDate today = LocalDate.now();
		AcademicYearStatus status = calculateStatus(academicYear, today);
		if (status == AcademicYearStatus.IN_PROGRESS ||
				status == AcademicYearStatus.BETWEEN_SEMESTERS) {
			throw new IllegalStateException(
					"Cannot deactivate academic year '" + academicYear.getAcademicYearName() +
							"' while it is ongoing (status: " + status + "). " +
							"It can only be deactivated after " + academicYear.getSecondSemesterEnd()
			);
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

	private AcademicYearStatus calculateStatus(Academic academic, LocalDate date) {
		if (date.isBefore(academic.getFirstSemesterStart())) {
			return AcademicYearStatus.UPCOMING;
		}
		if (date.isAfter(academic.getSecondSemesterEnd())) {
			return AcademicYearStatus.COMPLETED;
		}
		if (date.isAfter(academic.getFirstSemesterEnd()) &&
				date.isBefore(academic.getSecondSemesterStart())) {
			return AcademicYearStatus.BETWEEN_SEMESTERS;
		}
		return AcademicYearStatus.IN_PROGRESS;
	}


	private boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
		return !date.isBefore(start) && !date.isAfter(end);
	}

	private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
		return !start1.isAfter(end2) && !start2.isAfter(end1);
	}


	private boolean datesMatch(Academic a1, Academic a2) {
		return a1.getFirstSemesterStart().equals(a2.getFirstSemesterStart()) &&
				a1.getFirstSemesterEnd().equals(a2.getFirstSemesterEnd()) &&
				a1.getSecondSemesterStart().equals(a2.getSecondSemesterStart()) &&
				a1.getSecondSemesterEnd().equals(a2.getSecondSemesterEnd());
	}
	private void validateSemesterGap(LocalDate firstEnd, LocalDate secondStart) {
		if (!firstEnd.isBefore(secondStart)) {
			throw new IllegalArgumentException(
					"First semester must end before second semester starts. " +
							"There should be at least one day gap."
			);
		}
	}
}