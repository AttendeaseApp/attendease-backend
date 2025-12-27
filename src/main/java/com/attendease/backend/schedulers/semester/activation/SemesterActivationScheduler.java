package com.attendease.backend.schedulers.semester.activation;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.enums.academic.Semester;
import com.attendease.backend.repository.academic.AcademicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Scheduler for automatically activating academic year semesters based on date.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SemesterActivationScheduler {

	private final AcademicRepository academicRepository;

	/**
	 * Runs daily at midnight to check and update the current semester.
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	@Transactional
	public void updateCurrentSemester() {
		log.info("Running semester activation check...");
		Optional<Academic> activeYearOpt = academicRepository.findByIsActive(true);

		if (activeYearOpt.isEmpty()) {
			log.warn("No active academic year found. Skipping semester activation check.");
			return;
		}

		Academic activeYear = activeYearOpt.get();
		LocalDate today = LocalDate.now();
		Semester newSemester = determineCurrentSemester(today, activeYear);
		Semester currentSemester = activeYear.getCurrentSemester();

		if (currentSemester != newSemester) {
			activeYear.setCurrentSemester(newSemester);
			academicRepository.save(activeYear);

			log.info("Semester updated for academic year '{}': {} -> {}",
					activeYear.getAcademicYearName(),
					currentSemester != null ? currentSemester.getDisplayName() : "None",
					newSemester != null ? newSemester.getDisplayName() : "None");
		} else {
			log.debug("Current semester '{}' remains unchanged for academic year '{}'",
					currentSemester != null ? currentSemester.getDisplayName() : "None",
					activeYear.getAcademicYearName());
		}
	}

	/*
	* PRIVATE HELPERS
	*/

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
