package com.attendease.backend.schedulers.academic;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.enums.academic.AcademicYearStatus;
import com.attendease.backend.domain.enums.academic.Semester;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.history.SectionHistory;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import com.attendease.backend.repository.academic.AcademicRepository;
import com.attendease.backend.repository.section.SectionRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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
	private final SectionRepository sectionRepository;
	private final StudentRepository studentRepository;
	private final UserRepository userRepository;

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

		activateSectionsForCurrentSemester(activeYear);
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
				deactivateStudentsForAcademicYear(activeYear);
				deactivateSectionsForAcademicYear(activeYear);
			} catch (IllegalStateException e) {
				log.warn("Could not deactivate completed academic year '{}': {}", activeYear.getAcademicYearName(), e.getMessage());
			}
		}
	}

	private void deactivateStudentsForAcademicYear(Academic academic) {
		List<Section> completedSections = sectionRepository.findBySemesterIn(List.of(Semester.FIRST.getNumber(), Semester.SECOND.getNumber()));
		if (completedSections.isEmpty()) {
			log.debug("No sections found, skipping student deactivation");
			return;
		}
		int totalDeactivated = 0;
		for (Section section : completedSections) {
			List<Students> studentsInSection = studentRepository.findBySection(section);
			for (Students student : studentsInSection) {
				archiveSectionToHistory(student, section, academic);
				User user = student.getUser();
				if (user.getAccountStatus() == AccountStatus.ACTIVE) {
					user.setAccountStatus(AccountStatus.INACTIVE);
					user.setUpdatedBy(String.valueOf(UserType.SYSTEM));
					userRepository.save(user);
					totalDeactivated++;
					log.debug("Deactivated student: {} ({})", student.getStudentNumber(), student.getUser().getEmail());
				}
			}
		}
		log.info("Deactivated {} student accounts for completed academic year '{}'", totalDeactivated, academic.getAcademicYearName());
	}

	private void deactivateSectionsForAcademicYear(Academic academic) {
		List<Section> sectionsToDeactivate = sectionRepository.findBySemesterIn(List.of(Semester.FIRST.getNumber(), Semester.SECOND.getNumber()));

		if (sectionsToDeactivate.isEmpty()) {
			log.debug("No sections found to deactivate for academic year '{}'", academic.getAcademicYearName());
			return;
		}

		sectionsToDeactivate.forEach(section -> section.setIsActive(false));
		sectionRepository.saveAll(sectionsToDeactivate);

		log.info("All sections deactivated for academic year '{}'", academic.getAcademicYearName());
	}

	private void activateSectionsForCurrentSemester(Academic activeYear) {
		if (activeYear.getCurrentSemester() == null) {
			log.debug("No current semester set for active academic year '{}'", activeYear.getAcademicYearName());
			return;
		}

		int currentSemesterNumber = activeYear.getCurrentSemester().getNumber();

		List<Section> inactiveSections = sectionRepository.findBySemesterAndIsActiveFalse(currentSemesterNumber);

		if (inactiveSections.isEmpty()) {
			log.debug("No inactive sections found for semester {}", currentSemesterNumber);
			return;
		}

		inactiveSections.forEach(section -> section.setIsActive(true));
		sectionRepository.saveAll(inactiveSections);

		log.info("Activated {} sections for semester {} of academic year '{}'", inactiveSections.size(), currentSemesterNumber, activeYear.getAcademicYearName());
	}

	private void archiveSectionToHistory(Students student, Section section, Academic academic) {
		if (student.getSectionHistory() == null) {
			student.setSectionHistory(new ArrayList<>());
		}

		boolean alreadyArchived = student.getSectionHistory().stream()
				.anyMatch(history ->
						history.getSectionId().equals(section.getId()) &&
								history.getAcademicYearId().equals(academic.getId())
				);

		if (!alreadyArchived) {
			SectionHistory history = SectionHistory.builder()
					.sectionId(section.getId())
					.sectionName(section.getSectionName())
					.academicYearId(academic.getId())
					.academicYearName(academic.getAcademicYearName())
					.yearLevel(String.valueOf(student.getYearLevel()))
					.startDate(academic.getFirstSemesterStart())
					.endDate(academic.getSecondSemesterEnd())
					.build();
			student.getSectionHistory().add(history);
			studentRepository.save(student);
		}
	}
}