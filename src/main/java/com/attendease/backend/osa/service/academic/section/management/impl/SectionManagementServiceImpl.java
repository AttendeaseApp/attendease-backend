package com.attendease.backend.osa.service.academic.section.management.impl;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.osa.service.academic.section.management.SectionManagementService;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;

import java.util.List;
import java.util.Optional;

import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing academic sections.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-24
 */
@Service
@RequiredArgsConstructor
public final class SectionManagementServiceImpl implements SectionManagementService {

    private final CourseRepository courseRepository;
    private final SectionsRepository sectionsRepository;
    private final EventRepository eventRepository;
    private final StudentRepository studentsRepository;
    private final UserValidator userValidator;
    private final AcademicYearManagementService academicYearManagementService;

    @Override
    @Transactional
    public Sections addNewSection(String courseId, Sections section) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        Academic activeAcademicYear = academicYearManagementService.getActiveAcademicYear().orElseThrow(() ->
                new IllegalStateException("No active academic year found. Please set an active academic year first."));

        String newSectionName = section.getSectionName().trim();

        if (newSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }

        if (sectionsRepository.findBySectionName(newSectionName).isPresent()) {
            throw new IllegalArgumentException("Section with name '" + newSectionName + "' already exists");
        }

        validateSectionFormat(newSectionName, course.getCourseName());

        section.setSectionName(newSectionName);
        section.setCourse(course);
        section.setAcademicYear(activeAcademicYear);
        section.calculateYearLevelAndSemester();
        section.validateSemesterMatchesAcademicYear();

        return sectionsRepository.save(section);
    }

    @Override
    public List<Sections> getSectionsByCourse(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found."));
        return sectionsRepository.findByCourse(course);
    }

    @Override
    public List<Sections> getSectionsByYearLevel(Integer yearLevel) {
        return sectionsRepository.findByYearLevel(yearLevel);
    }

    @Override
    public List<Sections> getSectionsBySemester(Integer semester) {
        return sectionsRepository.findBySemester(semester);
    }

    @Override
    public List<Sections> getSectionsByYearLevelAndSemester(Integer yearLevel, Integer semester) {
        return sectionsRepository.findByYearLevelAndSemester(yearLevel, semester);
    }

    @Override
    public List<Sections> getAllSections() {
        return sectionsRepository.findAll();
    }

    @Override
    public Sections getSectionById(String id) {
        return sectionsRepository.findById(id).orElseThrow(() -> new RuntimeException("Section not found."));
    }

    @Override
    public Optional<Sections> getSectionByFullName(String fullName) {
        userValidator.validateFullCourseSectionFormat(fullName);
        return sectionsRepository.findBySectionName(fullName);
    }

    @Override
    @Transactional
    public Sections updateSection(String id, Sections updatedSection) {
        Sections existing = getSectionById(id);
        String updatedSectionName = updatedSection.getSectionName().trim();

        if (updatedSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }

        if (existing.getSectionName().equals(updatedSectionName)) {
            return existing;
        }

        sectionsRepository.findBySectionName(updatedSectionName).ifPresent(s -> {
            if (!s.getId().equals(existing.getId())) {
                throw new IllegalArgumentException("A section with the name '" + updatedSectionName + "' already exists");
            }
        });

        validateSectionFormat(updatedSectionName, existing.getCourse().getCourseName());

        existing.setSectionName(updatedSectionName);
        existing.calculateYearLevelAndSemester();
        existing.validateSemesterMatchesAcademicYear();

        return sectionsRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteSection(String id) {
        Sections section = getSectionById(id);
        Long studentCount = studentsRepository.countBySection(section);
        Long eventCountById = eventRepository.countByEligibleStudentsSectionsContaining(section.getId());
        Long eventCountByName = eventRepository.countByEligibleStudentsSectionNamesContaining(section.getSectionName());
        Long totalEventCount = eventCountById + eventCountByName;

        if (studentCount > 0 || totalEventCount > 0) {
            String sectionName = section.getSectionName();
            StringBuilder message = new StringBuilder("Cannot delete section '" + sectionName + "' due to existing dependencies (")
                    .append(studentCount).append(" student");

            if (totalEventCount > 0) {
                message.append(", ").append(totalEventCount).append(" event sessions)");
            } else {
                message.append(")");
            }

            message.append(". Reassign or remove dependencies first.");
            throw new IllegalStateException(message.toString());
        }

        sectionsRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void createDefaultSections(String courseId) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        Academic activeAcademicYear = academicYearManagementService.getActiveAcademicYear()
                .orElseThrow(() -> new IllegalStateException("No active academic year found. Please set an active academic year first."));

        Integer currentSemester = activeAcademicYear.getCurrentSemester() != null
                ? activeAcademicYear.getCurrentSemester().getNumber()
                : null;

        if (currentSemester == null) {
            throw new IllegalStateException("Active academic year does not have a current semester set");
        }

        for (int yearLevel = 1; yearLevel <= 4; yearLevel++) {
            int sectionNumber = calculateSectionNumber(yearLevel, currentSemester);
            String fullSectionName = course.getCourseName() + "-" + sectionNumber;

            if (sectionsRepository.findBySectionName(fullSectionName).isEmpty()) {
                Sections section = Sections.builder()
                        .sectionName(fullSectionName)
                        .yearLevel(yearLevel)
                        .semester(currentSemester)
                        .course(course)
                        .academicYear(activeAcademicYear)
                        .build();

                sectionsRepository.save(section);
            }
        }
    }

    @Override
    @Transactional
    public void updateSectionsForCourseNameChange(String courseId, String newCourseName) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        List<Sections> sections = getSectionsByCourse(courseId);
        String oldCourseName = course.getCourseName();

        for (Sections section : sections) {
            String oldSectionName = section.getSectionName();
            String sectionNumber = extractSectionNumber(oldSectionName, oldCourseName);
            String newSectionName = newCourseName + "-" + sectionNumber;

            section.setSectionName(newSectionName);
            section.calculateYearLevelAndSemester();
            sectionsRepository.save(section);
        }
    }

    @Override
    public String generateSectionNumber(Integer yearLevel, Integer semester, Integer subNumber) {
        if (yearLevel < 1 || yearLevel > 4) {
            throw new IllegalArgumentException("Year level must be between 1 and 4");
        }
        if (semester < 1 || semester > 2) {
            throw new IllegalArgumentException("Semester must be 1 or 2");
        }
        if (subNumber < 1 || subNumber > 99) {
            throw new IllegalArgumentException("Sub-number must be between 1 and 99");
        }

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

        return String.format("%d%02d", firstDigit, subNumber);
    }

    /*
    * PRIVATE HELPERS
    */

    private void validateSectionFormat(String fullSectionName, String courseName) {
        userValidator.validateFullCourseSectionFormat(fullSectionName);

        String sectionNumber = extractSectionNumber(fullSectionName, courseName);

        if (sectionNumber.length() != 3) {
            throw new IllegalArgumentException(
                    "Section number must be exactly 3 digits. Example: " + courseName + "-101");
        }

        try {
            int number = Integer.parseInt(sectionNumber);
            int firstDigit = number / 100;

            if (firstDigit < 1 || firstDigit > 8) {
                throw new IllegalArgumentException(
                        "Invalid section number. First digit must be 1-8 representing year/semester. " +
                                "Valid patterns: 1XX (Y1S1), 2XX (Y1S2), 3XX (Y2S1), 4XX (Y2S2), " +
                                "5XX (Y3S1), 6XX (Y3S2), 7XX (Y4S1), 8XX (Y4S2)");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Section number must contain only digits");
        }
    }

    private String extractSectionNumber(String fullSectionName, String courseName) {
        String expectedPrefix = courseName + "-";
        if (!fullSectionName.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "Section name '" + fullSectionName + "' does not belong to course '" +
                            courseName + "'. Must start with '" + expectedPrefix + "'");
        }
        return fullSectionName.substring(expectedPrefix.length());
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
        return firstDigit * 100 + 1; // Default to XX01
    }
}