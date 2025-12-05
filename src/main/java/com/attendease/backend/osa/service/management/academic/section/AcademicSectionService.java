package com.attendease.backend.osa.service.management.academic.section;

import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@code AcademicSectionService} is a service layer for managing academic sections (e.g., "BSIT-101", "BSA-101", "BSECE-101").
 *
 * <p>This service provides CRUD operations for sections, with strict validation on naming format
 * ("COURSE_NAME-SECTION_NUMBER", e.g., "BSIT-101"). It supports auto-creation of defaults,
 * bulk operations for courses, and cascading updates/deletes. Integrates with
 * {@link CourseRepository} for parent course resolution.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-11-25
 */
@Service
@RequiredArgsConstructor
public class AcademicSectionService {

    private final CourseRepository courseRepository;
    private final SectionsRepository sectionsRepository;
    private final EventSessionsRepository eventSessionsRepository;
    private final StudentRepository studentsRepository;
    private final UserValidator userValidator;

    /**
     * Creates a new section under a specific course.
     *
     * <p>Validates the full name format and prefix match before saving.</p>
     *
     * @param courseId The ID of the parent course.
     * @param section The {@link Sections} entity to create (must have a valid {@code name}).
     * @return The saved {@link Sections} entity (with auto-generated ID and timestamps).
     *
     * @throws RuntimeException If the course is not found.
     * @throws IllegalArgumentException If the name format is invalid or mismatches the course.
     */
    public Sections createSection(String courseId, Sections section) {
        Courses course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        String newSectionName = section.getSectionName().trim();

        if (newSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }

        if (sectionsRepository.findBySectionName(newSectionName).isPresent()) {
            throw new IllegalArgumentException("Section with name '" + newSectionName + "' already exists");
        }

        validateFullSectionName(newSectionName, course.getCourseName());
        section.setSectionName(newSectionName);
        section.setCourse(course);
        return sectionsRepository.save(section);
    }

    /**
     * Retrieves all sections under a specific course.
     *
     * @param courseId The ID of the parent course.
     * @return A {@link List} of {@link Sections} for the course.
     *
     * @throws RuntimeException If the course is not found.
     */
    public List<Sections> getSectionsByCourse(String courseId) {
        Courses course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found."));
        return sectionsRepository.findByCourse(course);
    }

    /**
     * Retrieves all sections across all courses.
     *
     * @return A {@link List} of all {@link Sections} entities.
     */
    public List<Sections> getAllSections() {
        return sectionsRepository.findAll();
    }

    /**
     * Retrieves a section by its ID.
     *
     * @param id The unique ID of the section.
     * @return The {@link Sections} entity if found.
     *
     * @throws RuntimeException If the section is not found.
     */
    public Sections getSectionById(String id) {
        return sectionsRepository.findById(id).orElseThrow(() -> new RuntimeException("Section not found."));
    }

    /**
     * Retrieves a section by its full name (e.g., "BSIT-101").
     *
     * <p>Validates the format before querying.</p>
     *
     * @param fullName The full section name.
     * @return An {@link Optional} containing the {@link Sections} if found.
     *
     * @throws IllegalArgumentException If the name format is invalid.
     */
    public Optional<Sections> getSectionByFullName(String fullName) {
        userValidator.validateFullCourseSectionFormat(fullName);
        return sectionsRepository.findBySectionName(fullName);
    }

    /**
     * Updates an existing section by ID.
     *
     * <p>Only the {@code name} is updated. Validates the new name against the parent course.</p>
     *
     * @param id The unique ID of the section to update.
     * @param updatedSection The updated details (only {@code name} is applied).
     * @return The updated {@link Sections} entity (with refreshed timestamps).
     *
     * @throws RuntimeException If the section is not found.
     * @throws IllegalArgumentException If the new name format is invalid or mismatches the course.
     */
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
                throw new IllegalArgumentException("A section with the name '" + updatedSectionName + "' already exists. " + "Each section name must be unique.");
            }
        });

        validateFullSectionName(updatedSectionName, existing.getCourse().getCourseName());
        existing.setSectionName(updatedSectionName);
        return sectionsRepository.save(existing);
    }


    /**
     * Deletes a section by its ID **only if no dependencies exist**.
     *
     * <p>Prevents deletion if event sessions or students reference the section. Counts dependencies
     * and throws a detailed exception with counts and rationale (similar to attendance checks in event deletion).</p>
     *
     * @param id The unique ID of the section to delete.
     *
     * @throws IllegalStateException If the section is not found or dependencies exist (with detailed message including counts).
     */
    public void deleteSection(String id) {
        Sections section = getSectionById(id);
        Long studentCount = studentsRepository.countBySection(section);
        Long eventCountById = eventSessionsRepository.countByEligibleStudentsSectionsContaining(section.getId());
        Long eventCountByName = eventSessionsRepository.countByEligibleStudentsSectionNamesContaining(section.getSectionName());
        Long totalEventCount = eventCountById + eventCountByName;

        if (studentCount > 0 || eventCountById > 0 || eventCountByName > 0) {
            String sectionName = section.getSectionName();
            StringBuilder message = new StringBuilder("You cannot delete section '" + sectionName + "' due to existing dependencies (").append(studentCount).append(" students");
            if (eventCountById > 0 || eventCountByName > 0) {
                message.append(", ").append(totalEventCount).append(" event sessions (").append(eventCountById).append(" by ID, ").append(eventCountByName).append(" by name; possible overlap)").append(")");
            } else {
                message.append(")");
            }
            message.append(". This action is prevented to protect data integrity and avoid orphaned references. ").append("Reassign or remove dependencies first (e.g., re-enroll students or update event eligibility criteria).");
            throw new IllegalStateException(message.toString());
        }
        sectionsRepository.deleteById(id);
    }

    /**
     * Creates default sections for a given course.
     *
     * <p>Generates sections "COURSE_NAME-101" to "COURSE_NAME-801" if they don't exist.
     * Skips duplicates based on full name.</p>
     *
     * @param course The parent {@link Courses} entity.
     */
    public void createDefaultSections(Courses course) {
        List<String> defaultSectionNumbers = Arrays.asList("101", "201", "301", "401", "501", "601", "701", "801");
        String coursePrefix = course.getCourseName() + "-";
        for (String sectionNumber : defaultSectionNumbers) {
            String fullSectionName = coursePrefix + sectionNumber;
            if (sectionsRepository.findBySectionName(fullSectionName).isEmpty()) {
                Sections defaultSection = Sections.builder().sectionName(fullSectionName).course(course).build();
                createSection(course.getId(), defaultSection);
            }
        }
    }

    /**
     * Updates all sections for a course when its name changes.
     *
     * <p>Rebuilds section names with the new course prefix (e.g., "OLD-101" → "NEW-101").</p>
     *
     * @param courseId The ID of the course.
     * @param newCourseName The new course name to use as prefix.
     *
     * @throws RuntimeException If the course is not found.
     */
    public void updateSectionsForCourseNameChange(String courseId, String newCourseName) {
        Courses course = courseRepository.findById(courseId).orElseThrow();
        List<Sections> sections = getSectionsByCourse(courseId);
        String newPrefix = newCourseName + "-";
        for (Sections section : sections) {
            String oldNumber = section.getSectionName().substring(course.getCourseName().length() + 1);
            section.setSectionName(newPrefix + oldNumber);
            sectionsRepository.save(section);
        }
    }

    /**
     * Validates the full section name against the course name and default numbers.
     *
     * <p>Private helper; checks prefix match and allowed section numbers (101-801).</p>
     *
     * @param fullSectionName The full name to validate (e.g., "BSIT-101").
     * @param courseName The expected course prefix (e.g., "BSIT").
     *
     * @throws IllegalArgumentException If prefix mismatches, number invalid, or format wrong.
     */
    private void validateFullSectionName(String fullSectionName, String courseName) {
        userValidator.validateFullCourseSectionFormat(fullSectionName);
        final String sectionNumber = getString(fullSectionName, courseName);
        if (!Arrays.asList("101", "201", "301", "401", "501", "601", "701", "801").contains(sectionNumber)) {
            throw new IllegalArgumentException(
                    "Invalid section number. Allowed section numbers are: 101, 201, 301, 401, 501, 601, 701, 801."
            );
        }
    }

    private String getString(String fullSectionName, String courseName) {
        String expectedPrefix = courseName + "-";
        if (!fullSectionName.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "Section name '" + fullSectionName + "' does not belong to course '" + courseName + "'. " +
                            "All section names for this course must start with '" + expectedPrefix + "'. " +
                            "Example: " + expectedPrefix + "101"
            );
        }
        return fullSectionName.substring(expectedPrefix.length());
    }
}