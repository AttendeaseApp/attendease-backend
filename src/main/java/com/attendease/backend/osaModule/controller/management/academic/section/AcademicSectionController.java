package com.attendease.backend.osaModule.controller.management.academic.section;

import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.osaModule.service.management.academic.section.AcademicSectionService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code AcademicSectionController} id used for managing academic sections.
 *
 * <p>This controller provides CRUD operations for courses, ensuring that all endpoints are secured
 * for OSA (Office of Student Affairs) role users only</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-11-23
 */
@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class AcademicSectionController {

    private final AcademicSectionService academicSectionService;

    /**
     * Retrieves a section by its ID or all.
     * @param sectionId The ID of the section (query param).
     *
     * get specific section with id: {{localhost}}/api/sections?sectionId=6922c12a5034077d9784abaa
     * to get all sections: {{localhost}}/api/sections
     *
     * @return The section if found.
     */
    @GetMapping
    public ResponseEntity<?> getSectionByIdOrAll(@RequestParam(required = false) String sectionId) {
        if (sectionId != null && !sectionId.isEmpty()) {
            Sections section = academicSectionService.getSectionById(sectionId);
            return ResponseEntity.ok(section);
        } else {
            List<Sections> sections = academicSectionService.getAllSections();
            return ResponseEntity.ok(sections);
        }
    }

    /**
     * Creates a new section for a specific course.
     *
     * @param courseId The ID of the course.
     * @param section The section details (name should be full format, e.g., "BSECE-101").
     * @return The created section.
     */
    @PostMapping("/courses/{courseId}")
    public ResponseEntity<Sections> createSection(@PathVariable String courseId, @RequestBody Sections section) {
        Sections createdSection = academicSectionService.createSection(courseId, section);
        return new ResponseEntity<>(createdSection, HttpStatus.CREATED);
    }

    /**
     * Retrieves all sections for a specific course.
     *
     * @param courseId The ID of the course.
     * @return List of sections for the course.
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<Sections>> getSectionsByCourse(@PathVariable String courseId) {
        List<Sections> sections = academicSectionService.getSectionsByCourse(courseId);
        return ResponseEntity.ok(sections);
    }

    /**
     * Retrieves a section by its full name (e.g., "BSECE-101").
     *
     * @param sectionName The full section name.
     * @return The section if found.
     */
    @GetMapping("/full/{sectionName}")
    public ResponseEntity<Sections> getSectionByFullName(@PathVariable String sectionName) {
        Optional<Sections> optionalSection = academicSectionService.getSectionByFullName(sectionName);
        return optionalSection.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing section by ID.
     *
     * @param id The ID of the section to update.
     * @param updatedSection The updated section details.
     * @return The updated section.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Sections> updateSection(@PathVariable String id, @RequestBody Sections updatedSection) {
        Sections updated = academicSectionService.updateSection(id, updatedSection);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a section by ID.
     *
     * @param id The ID of the section to delete.
     * @return No content (204).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable String id) {
        academicSectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all sections for a specific course (admin-only, use with caution).
     *
     * @param courseId The ID of the course.
     * @return No content (204).
     */
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteSectionsByCourse(@PathVariable String courseId) {
        academicSectionService.deleteSectionsByCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}
