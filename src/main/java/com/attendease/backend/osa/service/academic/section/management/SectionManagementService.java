package com.attendease.backend.osa.service.academic.section.management;

import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.osa.service.academic.course.management.impl.CourseManagementServiceImpl;
import com.attendease.backend.repository.course.CourseRepository;

import java.util.List;
import java.util.Optional;


/**
 * {@code SectionManagementService} is a service layer for managing academic sections within Rogationist College - College Department (e.g., "BSIT-101", "BSA-101", "BSECE-101").
 *
 * <p>This service provides CRUD operations for sections, with strict validation on naming format
 * ("COURSE_NAME-SECTION_NUMBER", e.g., "BSIT-101"). It supports auto-creation of defaults,
 * bulk operations for courses, and cascading updates/deletes. Integrates with
 * {@link CourseRepository} for parent course resolution.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-25
 */
public interface SectionManagementService {

    /**
     * {@code addNewSection} is used to create a new section under a specific course.
     *
     * <p>Validates the full name format and prefix match before saving.</p>
     *
     * @param courseId The ID of the parent course.
     * @param section The {@link Sections} entity to create (must have a valid {@code name}).
     * @return The saved {@link Sections} entity (with auto-generated ID and timestamps).
     */
    Sections addNewSection(String courseId, Sections section);

    /**
     * {@code getSectionsByCourse} is used to retrieve all sections under a specific course.
     *
     * @param courseId The ID of the parent course.
     * @return A {@link List} of {@link Sections} for the course.
     */
    List<Sections> getSectionsByCourse(String courseId);

    List<Sections> getSectionsByYearLevel(Integer yearLevel);

    List<Sections> getSectionsBySemester(Integer semester);

    List<Sections> getSectionsByYearLevelAndSemester(Integer yearLevel, Integer semester);

    /**
     * {@code getAllSections} is used to retrieve all sections across all courses.
     *
     * @return A {@link List} of all {@link Sections} entities.
     */
    List<Sections> getAllSections();

    /**
     * {@code getSectionById} is used to retrieve a section by its ID.
     *
     * @param id The unique ID of the section.
     * @return The {@link Sections} entity if found.
     */
    Sections getSectionById(String id);

    /**
     * {@code getSectionByFullName} is used to retrieve a section by its full name (e.g., "BSIT-101").
     *
     * <p>Validates the format before querying.</p>
     *
     * @param fullName The full section name.
     * @return An {@link Optional} containing the {@link Sections} if found.
     */
    Optional<Sections> getSectionByFullName(String fullName);

    /**
     * {@code updateSection} is used to update an existing section by ID.
     *
     * <p>Only the {@code name} is updated. Validates the new name against the parent course.</p>
     *
     * @param id The unique ID of the section to update.
     * @param updatedSection The updated details (only {@code name} is applied).
     * @return The updated {@link Sections} entity (with refreshed timestamps).
     */
    Sections updateSection(String id, Sections updatedSection);

    /**
     * {@code deleteSection} is used to delete a section by its ID **only if no dependencies exist**.
     *
     * <p>Prevents deletion if event sessions or student reference the section. Counts dependencies
     * and throws a detailed exception with counts and rationale (similar to attendance checks in event deletion).</p>
     *
     * @param id The unique ID of the section to delete.
     */
    void deleteSection(String id);

    void createDefaultSections(String courseId);

    /**
     * {@code updateSectionsForCourseNameChange} is used by services {@link CourseManagementServiceImpl}
     * to update all sections for a course when its name changes.
     *
     * <p>Rebuilds section names with the new course prefix (e.g., "OLD-101" → "NEW-101").</p>
     *
     * @param courseId The ID of the course.
     * @param newCourseName The new course name to use as prefix.
     */
    void updateSectionsForCourseNameChange(String courseId, String newCourseName);
}
