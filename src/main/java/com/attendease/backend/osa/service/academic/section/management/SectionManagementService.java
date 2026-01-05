package com.attendease.backend.osa.service.academic.section.management;

import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.section.management.BulkSectionRequest;
import com.attendease.backend.domain.section.management.BulkSectionResult;
import com.attendease.backend.domain.section.management.SectionResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing academic sections.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-25
 */
public interface SectionManagementService {

    /**
     * Adds a new section to a course.
     *
     * @param courseId the ID of the course
     * @param section the section to add
     * @return the created section response
     */
    SectionResponse addNewSection(String courseId, Section section);

    BulkSectionResult addSectionsBulk(String courseId, List<BulkSectionRequest> requests);

    /**
     * Retrieves all sections for a specific course.
     *
     * @param courseId the ID of the course
     * @return list of section responses
     */
    List<SectionResponse> getSectionsByCourse(String courseId);

    /**
     * Retrieves all sections for a specific year level.
     *
     * @param yearLevel the year level (1-4)
     * @return list of section responses
     */
    List<SectionResponse> getSectionsByYearLevel(Integer yearLevel);

    /**
     * Retrieves all sections for a specific semester.
     *
     * @param semester the semester (1 or 2)
     * @return list of section responses
     */
    List<SectionResponse> getSectionsBySemester(Integer semester);

    /**
     * Retrieves all sections for a specific year level and semester combination.
     *
     * @param yearLevel the year level (1-4)
     * @param semester the semester (1 or 2)
     * @return list of section responses
     */
    List<SectionResponse> getSectionsByYearLevelAndSemester(Integer yearLevel, Integer semester);

    /**
     * Retrieves all sections in the system.
     *
     * @return list of all section responses
     */
    List<SectionResponse> getAllSections();

    /**
     * Retrieves a section by its ID.
     *
     * @param id the section ID
     * @return the section response
     */
    SectionResponse getSectionById(String id);

    /**
     * Retrieves a section by its full name (e.g., "BSIT-101").
     *
     * @param fullName the full section name
     * @return optional section response
     */
    Optional<SectionResponse> getSectionByFullName(String fullName);

    /**
     * Updates an existing section.
     *
     * @param id the section ID
     * @param updatedSection the updated section data
     * @return the updated section response
     */
    SectionResponse updateSection(String id, Section updatedSection);

    /**
     * Deletes a section if it has no dependencies.
     *
     * @param id the section ID
     * @throws IllegalStateException if section has dependencies
     */
    void deleteSection(String id);

    /**
     * Updates section names when a course name changes.
     *
     * @param courseId the ID of the course
     * @param newCourseName the new course name
     */
    void updateSectionsForCourseNameChange(String courseId, String newCourseName);
}
