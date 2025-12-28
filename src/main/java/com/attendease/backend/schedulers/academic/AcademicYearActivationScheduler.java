package com.attendease.backend.schedulers.academic;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.enums.academic.Semester;
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
 * Scheduler for automatically managing academic year and semester activation based on dates.
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

	/**
	 * Runs daily at midnight to check and update academic year and semester status.
	 * execution order:
	 * 1. checks if current active year should be deactivated(ended)
	 * 2. checks if a new academic year should be activated(started)
	 * 3. updates current semester for active academic year
	 */
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
		LocalDate yearEnd = activeYear.getSecondSemesterEnd();
		if (today.isAfter(yearEnd)) {
			activeYear.setActive(false);
			activeYear.setCurrentSemester(null);
			academicRepository.save(activeYear);
			log.info("Academic year '{}' has ended and been deactivated (ended on {})", activeYear.getAcademicYearName(), yearEnd);
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

		for (Academic year : allYears) {
			if (year.isActive()) {
				continue;
			}

			LocalDate yearStart = year.getFirstSemesterStart();
			LocalDate yearEnd = year.getSecondSemesterEnd();

			if (!today.isBefore(yearStart) && !today.isAfter(yearEnd)) {
				year.setActive(true);
				year.setCurrentSemester(determineCurrentSemester(today, year));
				academicRepository.save(year);
				log.info("Academic year '{}' has been automatically activated (started on {})", year.getAcademicYearName(), yearStart);
				return;
			}
		}

		log.debug("No upcoming academic year found to activate");
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
		if (currentSemester != newSemester) {
			activeYear.setCurrentSemester(newSemester);
			academicRepository.save(activeYear);
			String oldSemesterName = currentSemester != null ? currentSemester.getDisplayName() : "None";
			String newSemesterName = newSemester != null ? newSemester.getDisplayName() : "None (Between Semesters)";
			log.info("Semester updated for academic year '{}': {} -> {}",
					activeYear.getAcademicYearName(),
					oldSemesterName,
					newSemesterName);
		} else {
			log.debug("Current semester '{}' remains unchanged for academic year '{}'",
					currentSemester != null ? currentSemester.getDisplayName() : "None",
					activeYear.getAcademicYearName());
		}
	}

	/**
	 * I added a manual trigger for testing purposes(can be called via controller).
	 */
	@Transactional
	public void manualTrigger() {
		log.info("Manual trigger initiated for academic year activation check");
		processAcademicYearActivation();
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
