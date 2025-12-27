package com.attendease.backend.osa.service.academic.course.management;

import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.osa.service.academic.section.management.impl.SectionManagementServiceImpl;

import java.util.List;

/**
 * {@code CourseManagementService} is a service layer responsible for managing academic courses within Rogationist College - College Department (e.g., "BSIT", "BSA", "BSECE").
 *
 * <p>This service provides CRUD operations for courses, integrated with cluster and section management.
 * Key features include: duplicate name prevention per cluster, auto-creation of default sections
 * on course creation, and cascading updates/deletes to sections. It delegates to
 * {@link SectionManagementServiceImpl} for section-related operations.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-19
 */
public interface CourseManagementService {

    /**
     * Used to create a new course under a specific cluster.
     *
     * <p>Validates uniqueness within the cluster, sets the cluster reference, saves the course,
     * and auto-creates default sections.</p>
     *
     * @param clusterId The ID of the parent cluster.
     * @param course The {@link Course} entity to create (must have a non-blank {@code courseName}).
     * @return The saved {@link Course} entity (with auto-generated ID and timestamps).
     */
    Course addNewCourse(String clusterId, Course course);

    /**
     * {@code getAllCourses} is used to retrieve all courses across all clusters.
     *
     * @return A {@link List} of all {@link Course} entities.
     */
    List<Course> getAllCourses();

    /**
     * Used to retrieve a course by its ID.
     *
     * @param id The unique ID of the course.
     * @return The {@link Course} entity if found.
     */
    Course getCourseById(String id);

    /**
     * Used to retrieve all courses under a specific cluster.
     *
     * @param clusterId The ID of the parent cluster.
     * @return A {@link List} of {@link Course} in the cluster.
     */
    List<Course> getCoursesByCluster(String clusterId);

    /**
     * Used to update an existing course by ID.
     *
     * <p>Only the {@code courseName} is updated. Cascades the name change to all associated sections.</p>
     *
     * @param id The unique ID of the course to update.
     * @param updatedCourse The updated details (only {@code courseName} is applied).
     * @return The updated {@link Course} entity (with refreshed timestamps).
     */
    Course updateCourse(String id, Course updatedCourse);

    /**
     * Used to delete a course by its ID, cascading to sections **only if sections have no dependencies
     * to
     * {@link Section},
     * {@link com.attendease.backend.domain.student.Students},
     * {@link Event}**.
     *
     * <p>Prevents deletion if event sessions reference the course directly. If sections exist, attempts to
     * cascade deletion to each section (which individually checks for student/event dependencies). If any
     * section cannot be deleted (e.g., has enrolled student or referenced events), throws a detailed exception
     * from the section deletion. Counts dependencies and provides rationale.</p>
     *
     * @param id The unique ID of the course to delete.
     */
    void deleteCourseById(String id);
}
