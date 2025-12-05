package com.attendease.backend.osa.controller.management.academic.course;

import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.osa.service.management.academic.course.ManagementAcademicCourseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * {@code ManagementAcademicCourseController} id used for managing academic courses.
 *
 * <p>This controller provides CRUD operations for courses, ensuring that all endpoints are secured
 * for OSA (Office of Student Affairs) role users only. Creating a course automatically generates
 * default sections (e.g., BSIT-101, BSIT-201, ..., BSIT-801) based on the course name.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-19
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class ManagementAcademicCourseController {

    private final ManagementAcademicCourseService managementAcademicCourseService;

    /**
     * Creates a new course associated with a specific cluster.
     *
     * <p>Upon creation, default sections are automatically generated (e.g., for course "BSIT",
     * sections like "BSIT-101", "BSIT-201", up to "BSIT-801" are created).</p>
     *
     * @param clusterId The ID of the parent cluster (required query parameter).
     * @param course The course details (validated; must include a non-blank {@code courseName}).
     * @return The created {@link Courses} entity (HTTP 200 OK).
     *
     * @throws IllegalArgumentException If the course name already exists in the cluster.
     * @throws RuntimeException If the cluster is not found.
     */
    @PostMapping
    public ResponseEntity<Courses> create(@RequestParam String clusterId, @RequestBody @Valid Courses course) {
        return ResponseEntity.ok(managementAcademicCourseService.createCourse(clusterId, course));
    }

    /**
     * Retrieves all courses across all clusters.
     *
     * @return A list of all {@link Courses} (HTTP 200 OK).
     */
    @GetMapping
    public ResponseEntity<List<Courses>> getAll() {
        return ResponseEntity.ok(managementAcademicCourseService.getAllCourses());
    }

    /**
     * Retrieves a specific course by its ID.
     *
     * @param id The unique ID of the course.
     * @return The {@link Courses} entity (HTTP 200 OK).
     *
     * @throws RuntimeException If the course is not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Courses> getById(@PathVariable String id) {
        return ResponseEntity.ok(managementAcademicCourseService.getCourseById(id));
    }

    /**
     * Retrieves all courses under a specific cluster.
     *
     * @param clusterId The ID of the cluster.
     * @return A list of {@link Courses} in the cluster (HTTP 200 OK).
     *
     * @throws RuntimeException If the cluster is not found.
     */
    @GetMapping("/cluster/{clusterId}")
    public ResponseEntity<List<Courses>> getByCluster(@PathVariable String clusterId) {
        return ResponseEntity.ok(managementAcademicCourseService.getCoursesByCluster(clusterId));
    }

    /**
     * Partially updates an existing course by ID (PATCH semantics).
     *
     * <p>Only the {@code courseName} is updated. Changing the name cascades to update all
     * referencing section names (e.g., "BSIT-101" becomes "IT-101"). Other fields
     * (e.g., cluster) are ignored.</p>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>{@code
     * {
     *   "courseName": "IT"
     * }
     * }</pre>
     *
     * @param id The unique ID of the course to update.
     * @param course The updated course details (only {@code courseName} is applied).
     * @return The updated {@link Courses} entity (HTTP 200 OK).
     *
     * @throws RuntimeException If the course is not found.
     * @throws IllegalArgumentException If the new name already exists in the cluster.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Courses> update(@PathVariable String id, @RequestBody Courses course) {
        return ResponseEntity.ok(managementAcademicCourseService.updateCourse(id, course));
    }

    /**
     * Deletes a course, conditionally cascading to sections.
     *
     * <p>This performs a conditional cascading delete: First checks for direct event dependencies on the course—if any exist,
     * deletion is prevented. If sections exist and have no dependencies (students or events), they are deleted first, then the course.
     * If any section has dependencies, deletion stops and throws a detailed integrity exception.</p>
     *
     * <p><strong>Responses:</strong></p>
     * <ul>
     *   <li><strong>204 No Content</strong>: Successful deletion (no dependencies or all resolved).</li>
     *   <li><strong>409 Conflict</strong>: Deletion prevented due to dependencies (e.g., enrolled students, referenced events)—check logs/response body for details.</li>
     *   <li><strong>404 Not Found</strong>: Course not found.</li>
     * </ul>
     *
     * @param id The unique ID of the course to delete.
     * @return No content (HTTP 204 No Content) on success.
     *
     * @throws RuntimeException If integrity checks fail (mapped to 409) or course not found (mapped to 404).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        managementAcademicCourseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
