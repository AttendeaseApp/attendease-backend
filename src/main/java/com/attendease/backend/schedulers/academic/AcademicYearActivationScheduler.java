package com.attendease.backend.schedulers.academic;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.enums.academic.AcademicYearStatus;
import com.attendease.backend.domain.enums.academic.Semester;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import com.attendease.backend.repository.academic.AcademicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Scheduler for automatically managing academic year and semester state transitions based on dates.
 * <p>
 * Handles:
 * <ul>
 *   <li>Automatic activation of upcoming academic years when they start</li>
 *   <li>Automatic deactivation of academic years when they end</li>
 *   <li>Automatic semester transitions within active academic years</li>
 * </ul>
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicYearActivationScheduler {

	private final AcademicRepository academicRepository;
	private final AcademicYearManagementService academicYearManagementService;

	@Scheduled(cron = "0 0 0 * * ?")
	@Transactional
	public void processAcademicYearActivation() {
		log.info("=== Starting academic year activation check ===");
		LocalDate today = LocalDate.now();

		try {
			deactivateEndedAcademicYear(today);
			activateUpcomingAcademicYear(today);
			updateCurrentSemester(today);
			log.info("=== Academic year activation check completed successfully ===");
		} catch (Exception e) {
			log.error("Error during academic year activation check", e);
		}
	}


	private void deactivateEndedAcademicYear(LocalDate today) {
		Optional<Academic> activeYearOpt = academicRepository.findByIsActive(true);
		if (activeYearOpt.isEmpty()) {
			log.debug("No active academic year to deactivate");
			return;
		}
		Academic activeYear = activeYearOpt.get();
		AcademicYearStatus status = calculateStatus(activeYear, today);

		if (status == AcademicYearStatus.COMPLETED) {
			try {
				academicYearManagementService.deactivateAcademicYear(activeYear.getId());
				log.info("Academic year '{}' has ended and been deactivated (ended on {})",
						activeYear.getAcademicYearName(),
						activeYear.getSecondSemesterEnd());
			} catch (IllegalStateException e) {
				log.warn("Could not deactivate completed academic year '{}': {}", activeYear.getAcademicYearName(), e.getMessage());
			}
		}
	}


	private void activateUpcomingAcademicYear(LocalDate today) {
		Optional<Academic> activeYearOpt = academicRepository.findByIsActive(true);
		if (activeYearOpt.isPresent()) {
			log.debug("An active academic year already exists: '{}'",
					activeYearOpt.get().getAcademicYearName());
			return;
		}
		List<Academic> allYears = academicRepository.findAll();
		Optional<Academic> yearToActivate = allYears.stream()
				.filter(year -> !year.isActive())
				.filter(year -> {
					AcademicYearStatus status = calculateStatus(year, today);
					return status == AcademicYearStatus.IN_PROGRESS ||
							status == AcademicYearStatus.BETWEEN_SEMESTERS;
				})
				.findFirst();

		if (yearToActivate.isPresent()) {
			Academic year = yearToActivate.get();
			try {
				academicYearManagementService.setActiveAcademicYear(year.getId());
				log.info("Academic year '{}' has been automatically activated (started on {})", year.getAcademicYearName(), year.getFirstSemesterStart());
			} catch (IllegalStateException e) {
				log.error("Failed to activate academic year '{}': {}", year.getAcademicYearName(), e.getMessage());
			}
		} else {
			log.debug("No upcoming academic year found to activate");
		}
	}


	private void updateCurrentSemester(LocalDate today) {
		Optional<Academic> activeYearOpt = academicRepository.findByIsActive(true);
		if (activeYearOpt.isEmpty()) {
			log.debug("No active academic year found. Skipping semester update.");
			return;
		}

		Academic activeYear = activeYearOpt.get();
		Semester newSemester = determineCurrentSemester(today, activeYear);
		Semester currentSemester = activeYear.getCurrentSemester();

		if (currentSemester != newSemester && newSemester != null) {
			activeYear.setCurrentSemester(newSemester);
			academicRepository.save(activeYear);

			String oldSemesterName = currentSemester != null ? currentSemester.getDisplayName() : "None";
			log.info("Semester updated for academic year '{}': {} -> {}",
					activeYear.getAcademicYearName(),
					oldSemesterName,
					newSemester.getDisplayName());
		}
	}


	@Transactional
	public void manualTrigger() {
		log.info("Manual trigger initiated for academic year activation check");
		processAcademicYearActivation();
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


	private Semester determineCurrentSemester(LocalDate today, Academic academic) {
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