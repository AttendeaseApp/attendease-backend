package com.attendease.backend.schedulers.academic;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.academic.AcademicYearStatus;
import com.attendease.backend.domain.enums.academic.Semester;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.history.SectionHistory;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.osa.service.academic.section.management.SectionManagementService;
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
	private final AcademicYearManagementService academicYearManagementService;
	private final SectionRepository sectionRepository;
	private final StudentRepository studentRepository;
	private final UserRepository userRepository;
	private final SectionManagementService sectionManagementService;

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
				deactivateSectionsForAcademicYear(activeYear);
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
				sectionManagementService.createAllSectionsForActiveAcademicYear();
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

		if(currentSemester != newSemester && newSemester != null){
			boolean isNewAcademicYear = (currentSemester == Semester.SECOND && newSemester == Semester.FIRST);
			transitionToSemester(activeYear, currentSemester, newSemester, isNewAcademicYear);
			activeYear.setCurrentSemester(newSemester);
			academicRepository.save(activeYear);
			String oldSemesterName = currentSemester != null ? currentSemester.getDisplayName() : "None";
			log.info("Semester updated for academic year '{}': {} -> {}",
					activeYear.getAcademicYearName(),
					oldSemesterName,
					newSemester.getDisplayName());
		}
	}


	private void deactivateSectionsForAcademicYear(Academic academicYear) {
		List<Section> sections = sectionRepository.findByAcademicYearAndIsActive(academicYear, true);
		int deactivatedCount = 0;
		for(Section section : sections){
			section.deactivate();
			sectionRepository.save(section);
			deactivatedCount++;
		}
		log.info("Deactivated {} sections for completed academic year '{}'", deactivatedCount, academicYear.getAcademicYearName());
	}


	private void transitionToSemester(Academic academicYear, Semester oldSemester, Semester newSemester, boolean isNewAcademicYear) {
		log.info("Transitioning sections from {} to {} for academic year '{}' (New Year: {})",
				oldSemester != null ? oldSemester.getDisplayName() : "None",
				newSemester.getDisplayName(),
				academicYear.getAcademicYearName(),
				isNewAcademicYear);

		if (oldSemester != null) {
			List<Section> oldSections = sectionRepository.findByAcademicYearAndSemesterAndIsActive(academicYear, oldSemester.getNumber(), true);
			for (Section section : oldSections) {
				section.deactivate();
				sectionRepository.save(section);
			}
			log.info("Deactivated {} sections for {}", oldSections.size(), oldSemester.getDisplayName());
		}

		List<Section> newSections = sectionRepository.findByAcademicYearAndSemester(academicYear, newSemester.getNumber());

		int activatedCount = 0;
		for (Section section : newSections) {
			if (!section.isCurrentlyActive()) {
				section.activate();
				sectionRepository.save(section);
				activatedCount++;
			}
		}

		log.info("Activated {} sections for {}", activatedCount, newSemester.getDisplayName());
		List<Students> allStudents = studentRepository.findAll();
		progressStudents(allStudents, academicYear, newSemester, isNewAcademicYear);
	}


	private void progressStudents(List<Students> students, Academic academicYear, Semester newSemester, boolean isNewAcademicYear) {
		int progressedCount = 0;
		int graduatedCount = 0;
		int failedCount = 0;

		for (Students student : students) {
			if (student.getSection() == null) {
				continue;
			}

			Section currentSection = student.getSection();
			addToSectionHistory(student, currentSection, academicYear);

			int newYearLevel;
			int newSemesterNumber = newSemester.getNumber();

			if (isNewAcademicYear) {
				newYearLevel = currentSection.getYearLevel() + 1;

				if (newYearLevel > 4) {
					handleGraduation(student, currentSection);
					graduatedCount++;
					continue;
				}
			} else {
				newYearLevel = currentSection.getYearLevel();
			}

			String courseName = currentSection.getCourse().getCourseName();
			int newSectionNumber = calculateSectionNumber(newYearLevel, newSemesterNumber);
			String newSectionName = courseName + "-" + newSectionNumber;

			Optional<Section> newSectionOpt = sectionRepository
					.findBySectionNameAndAcademicYear(newSectionName, academicYear);

			if (newSectionOpt.isPresent() && newSectionOpt.get().getIsActive()) {
				Section newSection = newSectionOpt.get();
				student.setSection(newSection);
				student.setCurrentSectionId(newSection.getId());
				student.setSectionName(newSection.getSectionName());
				student.setYearLevel(newYearLevel);
				studentRepository.save(student);
				progressedCount++;

				log.debug("Progressed student {} from {} to {}",
						student.getStudentNumber(),
						currentSection.getSectionName(),
						newSection.getSectionName());
			} else {
				log.error("Failed to find active section {} for student {}", newSectionName, student.getStudentNumber());
				failedCount++;
			}
		}
		log.info("Student progression complete: {} progressed, {} graduated, {} failed", progressedCount, graduatedCount, failedCount);
	}


	private void addToSectionHistory(Students student, Section section, Academic academicYear) {
		if (student.getSectionHistory() == null) {
			student.setSectionHistory(new ArrayList<>());
		}

		SectionHistory history = new SectionHistory();
		history.setSectionId(section.getId());
		history.setSectionName(section.getSectionName());
		history.setAcademicYearId(academicYear.getId());

		LocalDate endDate = section.getSemester() == 1 ? academicYear.getFirstSemesterEnd() : academicYear.getSecondSemesterEnd();
		history.setEndDate(endDate);

		List<SectionHistory> existingHistory = student.getSectionHistory();
		if (!existingHistory.isEmpty()) {
			SectionHistory lastHistory = existingHistory.getLast();
			history.setStartDate(lastHistory.getEndDate().plusDays(1));
		} else {
			LocalDate startDate = section.getSemester() == 1 ? academicYear.getFirstSemesterStart() : academicYear.getSecondSemesterStart();
			history.setStartDate(startDate);
		}

		student.getSectionHistory().add(history);
	}


	private void handleGraduation(Students student, Section currentSection) {
		log.info("Student {} has completed all year levels (last section: {})", student.getStudentNumber(), currentSection.getSectionName());
		student.setSection(null);
		student.setCurrentSectionId(null);
		student.setSectionName(null);
		student.setYearLevel(null);
		User user = student.getUser();
		if (user != null) {
			user.setAccountStatus(AccountStatus.GRADUATED);
			userRepository.save(user);
			log.info("Set account status to GRADUATED for user: {}", user.getEmail());
		}
		studentRepository.save(student);
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


	private int calculateSectionNumber(int yearLevel, int semester) {
		int firstDigit;
		if (yearLevel == 1) {
			firstDigit = semester == 1 ? 1 : 2;
		} else if (yearLevel == 2) {
			firstDigit = semester == 1 ? 3 : 4;
		} else if (yearLevel == 3) {
			firstDigit = semester == 1 ? 5 : 6;
		} else {
			firstDigit = semester == 1 ? 7 : 8;
		}
		return firstDigit * 100 + 1;
	}
}