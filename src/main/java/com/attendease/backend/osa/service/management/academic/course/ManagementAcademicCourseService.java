package com.attendease.backend.osa.service.management.academic.course;

import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.osa.service.management.academic.section.impl.ManagementAcademicSectionServiceImpl;

import java.util.List;

/**
 * {@code ManagementAcademicCourseService} is a service layer responsible for managing academic courses within Rogationist College - College Department (e.g., "BSIT", "BSA", "BSECE").
 *
 * <p>This service provides CRUD operations for courses, integrated with cluster and section management.
 * Key features include: duplicate name prevention per cluster, auto-creation of default sections
 * on course creation, and cascading updates/deletes to sections. It delegates to
 * {@link ManagementAcademicSectionServiceImpl} for section-related operations.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-19
 */
public interface ManagementAcademicCourseService {

    /**
     * {@code createCourse} is used to create a new course under a specific cluster.
     *
     * <p>Validates uniqueness within the cluster, sets the cluster reference, saves the course,
     * and auto-creates default sections.</p>
     *
     * @param clusterId The ID of the parent cluster.
     * @param course The {@link Courses} entity to create (must have a non-blank {@code courseName}).
     * @return The saved {@link Courses} entity (with auto-generated ID and timestamps).
     *
     * @throws RuntimeException If the cluster is not found.
     * @throws IllegalArgumentException If the course name already exists in the cluster.
     */
    Courses createCourse(String clusterId, Courses course);

    /**
     * {@code getAllCourses} is used to retrieve all courses across all clusters.
     *
     * @return A {@link List} of all {@link Courses} entities.
     */
    List<Courses> getAllCourses();

    /**
     * {@code getCourseById} is used to retrieve a course by its ID.
     *
     * @param id The unique ID of the course.
     * @return The {@link Courses} entity if found.
     *
     * @throws RuntimeException If the course is not found.
     */
    Courses getCourseById(String id);

    /**
     * {@code getCoursesByCluster} is used to retrieve all courses under a specific cluster.
     *
     * @param clusterId The ID of the parent cluster.
     * @return A {@link List} of {@link Courses} in the cluster.
     *
     * @throws RuntimeException If the cluster is not found.
     */
    List<Courses> getCoursesByCluster(String clusterId);

    /**
     * {@code updateCourse} is used to update an existing course by ID.
     *
     * <p>Only the {@code courseName} is updated. Cascades the name change to all associated sections.</p>
     *
     * @param id The unique ID of the course to update.
     * @param updatedCourse The updated details (only {@code courseName} is applied).
     * @return The updated {@link Courses} entity (with refreshed timestamps).
     *
     * @throws RuntimeException If the course is not found.
     */
    Courses updateCourse(String id, Courses updatedCourse);

    /**
     * {@code deleteCourse} is used to delete a course by its ID, cascading to sections **only if sections have no dependencies
     * to
     * {@link com.attendease.backend.domain.sections.Sections},
     * {@link com.attendease.backend.domain.students.Students},
     * {@link com.attendease.backend.domain.events.EventSessions}**.
     *
     * <p>Prevents deletion if event sessions reference the course directly. If sections exist, attempts to
     * cascade deletion to each section (which individually checks for student/event dependencies). If any
     * section cannot be deleted (e.g., has enrolled students or referenced events), throws a detailed exception
     * from the section deletion. Counts dependencies and provides rationale.</p>
     *
     * @param id The unique ID of the course to delete.
     *
     * @throws RuntimeException If the course is not found.
     * @throws IllegalStateException If direct event dependencies exist or cascade fails (with user-friendly message including student and event counts).
     */
    void deleteCourse(String id);
}
