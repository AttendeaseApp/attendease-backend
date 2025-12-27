package com.attendease.backend.osa.controller.academic.course.management;

import com.attendease.backend.domain.course.Course;
import com.attendease.backend.osa.service.academic.course.management.CourseManagementService;
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
 * for osa (Office of Student Affairs) role user only. Creating a course automatically generates
 * default sections (e.g., BSIT-101, BSIT-201, ..., BSIT-801) based on the course name.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-19
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class CourseManagementController {

    private final CourseManagementService courseManagementService;

    /**
     * Creates a new course associated with a specific cluster.
     *
     * <p>Upon creation, default sections are automatically generated (e.g., for course "BSIT",
     * sections like "BSIT-101", "BSIT-201", up to "BSIT-801" are created).</p>
     *
     * @param clusterId The ID of the parent cluster (required query parameter).
     * @param course The course details (validated; must include a non-blank {@code courseName}).
     * @return The created {@link Course}
     */
    @PostMapping
    public ResponseEntity<Course> addNewCourse(@RequestParam String clusterId, @RequestBody @Valid Course course) {
        return ResponseEntity.ok(courseManagementService.addNewCourse(clusterId, course));
    }

    /**
     * Retrieves all courses across all clusters.
     *
     * @return A list of all {@link Course} (HTTP 200 OK).
     */
    @GetMapping
    public ResponseEntity<List<Course>> getAll() {
        return ResponseEntity.ok(courseManagementService.getAllCourses());
    }

    /**
     * Retrieves a specific course by its ID.
     *
     * @param id The unique ID of the course.
     * @return The {@link Course} entity (HTTP 200 OK).
     *
     * @throws RuntimeException If the course is not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable String id) {
        return ResponseEntity.ok(courseManagementService.getCourseById(id));
    }

    /**
     * Retrieves all courses under a specific cluster.
     *
     * @param clusterId The ID of the cluster.
     * @return A list of {@link Course} in the cluster (HTTP 200 OK).
     */
    @GetMapping("/cluster/{clusterId}")
    public ResponseEntity<List<Course>> getByCluster(@PathVariable String clusterId) {
        return ResponseEntity.ok(courseManagementService.getCoursesByCluster(clusterId));
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
     * @return The updated {@link Course} entity (HTTP 200 OK).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable String id, @RequestBody Course course) {
        return ResponseEntity.ok(courseManagementService.updateCourse(id, course));
    }

    /**
     * Deletes a course, conditionally cascading to sections.
     *
     * <p>This performs a conditional cascading delete: First checks for direct event dependencies on the course—if any exist,
     * deletion is prevented. If sections exist and have no dependencies (student or events), they are deleted first, then the course.
     * If any section has dependencies, deletion stops and throws a detailed integrity exception.</p>
     *
     * @param id The unique ID of the course to delete.
     * @return No content (HTTP 204 No Content) on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourseById(@PathVariable String id) {
        courseManagementService.deleteCourseById(id);
        return ResponseEntity.noContent().build();
    }
}
