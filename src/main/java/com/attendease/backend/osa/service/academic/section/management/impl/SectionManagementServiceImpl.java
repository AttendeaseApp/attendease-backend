package com.attendease.backend.osa.service.academic.section.management.impl;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.osa.service.academic.section.management.SectionManagementService;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.section.SectionRepository;
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
    private final SectionRepository sectionRepository;
    private final EventRepository eventRepository;
    private final StudentRepository studentsRepository;
    private final UserValidator userValidator;
    private final AcademicYearManagementService academicYearManagementService;

    @Override
    @Transactional
    public Section addNewSection(String courseId, Section section) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        Academic activeAcademicYear = academicYearManagementService.getActiveAcademicYear().orElseThrow(() ->
                new IllegalStateException("No active academic year found. Please set an active academic year first."));

        String newSectionName = section.getSectionName().trim();

        if (newSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }

        if (sectionRepository.findBySectionName(newSectionName).isPresent()) {
            throw new IllegalArgumentException("Section with name '" + newSectionName + "' already exists");
        }

        validateSectionFormat(newSectionName, course.getCourseName());

        section.setSectionName(newSectionName);
        section.setCourse(course);
        section.setAcademicYear(activeAcademicYear);
        section.calculateYearLevelAndSemester();
        section.validateSemesterMatchesAcademicYear();

        return sectionRepository.save(section);
    }

    @Override
    public List<Section> getSectionsByCourse(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found."));
        return sectionRepository.findByCourse(course);
    }

    @Override
    public List<Section> getSectionsByYearLevel(Integer yearLevel) {
        return sectionRepository.findByYearLevel(yearLevel);
    }

    @Override
    public List<Section> getSectionsBySemester(Integer semester) {
        return sectionRepository.findBySemester(semester);
    }

    @Override
    public List<Section> getSectionsByYearLevelAndSemester(Integer yearLevel, Integer semester) {
        return sectionRepository.findByYearLevelAndSemester(yearLevel, semester);
    }

    @Override
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    @Override
    public Section getSectionById(String id) {
        return sectionRepository.findById(id).orElseThrow(() -> new RuntimeException("Section not found."));
    }

    @Override
    public Optional<Section> getSectionByFullName(String fullName) {
        userValidator.validateFullCourseSectionFormat(fullName);
        return sectionRepository.findBySectionName(fullName);
    }

    @Override
    @Transactional
    public Section updateSection(String id, Section updatedSection) {
        Section existing = getSectionById(id);
        String updatedSectionName = updatedSection.getSectionName().trim();

        if (updatedSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }

        if (existing.getSectionName().equals(updatedSectionName)) {
            return existing;
        }

        sectionRepository.findBySectionName(updatedSectionName).ifPresent(s -> {
            if (!s.getId().equals(existing.getId())) {
                throw new IllegalArgumentException("A section with the name '" + updatedSectionName + "' already exists");
            }
        });

        validateSectionFormat(updatedSectionName, existing.getCourse().getCourseName());

        existing.setSectionName(updatedSectionName);
        existing.calculateYearLevelAndSemester();
        existing.validateSemesterMatchesAcademicYear();

        return sectionRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteSection(String id) {
        Section section = getSectionById(id);
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

        sectionRepository.deleteById(id);
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

            if (sectionRepository.findBySectionName(fullSectionName).isEmpty()) {
                Section section = Section.builder()
                        .sectionName(fullSectionName)
                        .yearLevel(yearLevel)
                        .semester(currentSemester)
                        .course(course)
                        .academicYear(activeAcademicYear)
                        .build();

                sectionRepository.save(section);
            }
        }
    }

    @Override
    @Transactional
    public void updateSectionsForCourseNameChange(String courseId, String newCourseName) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        List<Section> sections = getSectionsByCourse(courseId);
        String oldCourseName = course.getCourseName();

        for (Section section : sections) {
            String oldSectionName = section.getSectionName();
            String sectionNumber = extractSectionNumber(oldSectionName, oldCourseName);
            String newSectionName = newCourseName + "-" + sectionNumber;

            section.setSectionName(newSectionName);
            section.calculateYearLevelAndSemester();
            sectionRepository.save(section);
        }
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