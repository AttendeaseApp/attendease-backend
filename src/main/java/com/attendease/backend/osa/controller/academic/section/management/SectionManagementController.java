package com.attendease.backend.osa.controller.academic.section.management;

import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.section.management.BulkSectionRequest;
import com.attendease.backend.domain.section.management.BulkSectionResult;
import com.attendease.backend.domain.section.management.SectionResponse;
import com.attendease.backend.osa.service.academic.section.management.SectionManagementService;

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
 * {@code SectionManagementController} is used for managing academic sections.
 *
 * <p>This controller provides CRUD operations for sections, ensuring that all endpoints are secured
 * for OSA (Office of Student Affairs) role users only.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-11-23
 */
@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class SectionManagementController {

    private final SectionManagementService sectionManagementService;

    /**
     * Retrieves a section by its ID or all sections.
     *
     * @param sectionId The ID of the section (query param).
     * @return The section if found, or list of all sections.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>Get specific section: GET /api/sections?sectionId=6922c12a5034077d9784abaa</li>
     *   <li>Get all sections: GET /api/sections</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<?> getSectionByIdOrAll(@RequestParam(required = false) String sectionId) {
        if (sectionId != null && !sectionId.isEmpty()) {
            SectionResponse section = sectionManagementService.getSectionById(sectionId);
            return ResponseEntity.ok(section);
        } else {
            List<SectionResponse> sections = sectionManagementService.getAllSections();
            return ResponseEntity.ok(sections);
        }
    }

    /**
     * Bulk create sections for a course
     * POST /api/osa/sections/bulk?courseId=THECOURSEID
     * sample body:
     * [
     *   {
     *     "sectionName": "BSCS-101",
     *     "yearLevel": 1,
     *     "semester": 1
     *   },
     *   {
     *     "sectionName": "BSCS-102",
     *     "yearLevel": 1,
     *     "semester": 1
     *   }
     * ]
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkSectionResult> createSectionsBulk(@RequestParam String courseId, @RequestBody List<BulkSectionRequest> requests) {
        BulkSectionResult result = sectionManagementService.addSectionsBulk(courseId, requests);
        if (result.getErrorCount() > 0 && result.getSuccessCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } else if (result.getErrorCount() > 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
    }

    /**
     * Activates a section based on the current semester.
     *
     * @param sectionId The ID of the section to activate.
     * @return The activated section response.
     */
    @PostMapping("/{sectionId}/activate")
    public ResponseEntity<?> activateSection(@PathVariable String sectionId) {
        try {
            SectionResponse activatedSection = sectionManagementService.activateSection(sectionId);
            return ResponseEntity.ok(activatedSection);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }


    /**
     * Creates a new section for a specific course.
     *
     * @param courseId The ID of the course.
     * @param section The section details (name should be full format, e.g., "BSECE-101").
     * @return The created section response.
     */
    @PostMapping("/courses/{courseId}")
    public ResponseEntity<SectionResponse> addNewSection(
            @PathVariable String courseId,
            @RequestBody Section section) {
        SectionResponse createdSection = sectionManagementService.addNewSection(courseId, section);
        return new ResponseEntity<>(createdSection, HttpStatus.CREATED);
    }

    /**
     * Retrieves all sections for a specific course.
     *
     * @param courseId The ID of the course.
     * @return List of section responses for the course.
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<SectionResponse>> getSectionsByCourse(@PathVariable String courseId) {
        List<SectionResponse> sections = sectionManagementService.getSectionsByCourse(courseId);
        return ResponseEntity.ok(sections);
    }

    /**
     * Retrieves sections by year level.
     *
     * @param yearLevel The year level (1-4).
     * @return List of section responses for the year level.
     */
    @GetMapping("/year-level/{yearLevel}")
    public ResponseEntity<List<SectionResponse>> getSectionsByYearLevel(@PathVariable Integer yearLevel) {
        List<SectionResponse> sections = sectionManagementService.getSectionsByYearLevel(yearLevel);
        return ResponseEntity.ok(sections);
    }

    /**
     * Retrieves sections by semester.
     *
     * @param semester The semester (1 or 2).
     * @return List of section responses for the semester.
     */
    @GetMapping("/semester/{semester}")
    public ResponseEntity<List<SectionResponse>> getSectionsBySemester(@PathVariable Integer semester) {
        List<SectionResponse> sections = sectionManagementService.getSectionsBySemester(semester);
        return ResponseEntity.ok(sections);
    }

    /**
     * Retrieves sections by year level and semester combination.
     *
     * @param yearLevel The year level (1-4).
     * @param semester The semester (1 or 2).
     * @return List of section responses matching the criteria.
     */
    @GetMapping("/year-level/{yearLevel}/semester/{semester}")
    public ResponseEntity<List<SectionResponse>> getSectionsByYearLevelAndSemester(
            @PathVariable Integer yearLevel,
            @PathVariable Integer semester) {
        List<SectionResponse> sections = sectionManagementService.getSectionsByYearLevelAndSemester(yearLevel, semester);
        return ResponseEntity.ok(sections);
    }

    /**
     * Retrieves a section by its full name (e.g., "BSECE-101").
     *
     * @param sectionName The full section name.
     * @return The section response if found.
     */
    @GetMapping("/full/{sectionName}")
    public ResponseEntity<SectionResponse> getSectionByFullName(@PathVariable String sectionName) {
        Optional<SectionResponse> optionalSection = sectionManagementService.getSectionByFullName(sectionName);
        return optionalSection
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing section by ID.
     *
     * @param id The ID of the section to update.
     * @param updatedSection The updated section details.
     * @return The updated section response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSection(@PathVariable String id, @RequestBody Section updatedSection) {
        try {
            SectionResponse updated = sectionManagementService.updateSection(id, updatedSection);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    /**
     * Deletes a section by ID.
     *
     * @param id The ID of the section to delete.
     * @return No content (204) if successful.
     * @throws IllegalStateException if section has dependencies (students or events).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSection(@PathVariable String id) {
        try {
            sectionManagementService.deleteSection(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
}