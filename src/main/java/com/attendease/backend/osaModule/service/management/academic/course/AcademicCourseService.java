package com.attendease.backend.osaModule.service.management.academic.course;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.osaModule.service.management.academic.section.AcademicSectionService;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@code AcademicCourseService} is a service layer for managing academic courses (e.g., "BSIT", "BSA", "BSECE").
 *
 * <p>This service provides CRUD operations for courses, integrated with cluster and section management.
 * Key features include: duplicate name prevention per cluster, auto-creation of default sections
 * on course creation, and cascading updates/deletes to sections. It delegates to
 * {@link AcademicSectionService} for section-related operations.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-19
 */
@Service
@RequiredArgsConstructor
public class AcademicCourseService {

    private final AcademicSectionService academicSectionService;
    private final CourseRepository courseRepository;
    private final ClustersRepository clusterRepository;

    /**
     * Creates a new course under a specific cluster.
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
    public Courses createCourse(String clusterId, Courses course) {
        Clusters cluster = clusterRepository.findById(clusterId).orElseThrow(() -> new RuntimeException("Cluster not found."));

        if (courseRepository.findByCourseNameAndCluster(course.getCourseName(), cluster).isPresent()) {
            throw new IllegalArgumentException("Course name '" + course.getCourseName() + "' already exists in this cluster.");
        }

        course.setCluster(cluster);
        Courses savedCourse = courseRepository.save(course);
        academicSectionService.createDefaultSections(savedCourse);
        return savedCourse;
    }

    /**
     * Retrieves all courses across all clusters.
     *
     * @return A {@link List} of all {@link Courses} entities.
     */
    public List<Courses> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Retrieves a course by its ID.
     *
     * @param id The unique ID of the course.
     * @return The {@link Courses} entity if found.
     *
     * @throws RuntimeException If the course is not found.
     */
    public Courses getCourseById(String id) {
        return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found."));
    }

    /**
     * Retrieves all courses under a specific cluster.
     *
     * @param clusterId The ID of the parent cluster.
     * @return A {@link List} of {@link Courses} in the cluster.
     *
     * @throws RuntimeException If the cluster is not found.
     */
    public List<Courses> getCoursesByCluster(String clusterId) {
        Clusters cluster = clusterRepository.findById(clusterId).orElseThrow(() -> new RuntimeException("Cluster not found."));
        return courseRepository.findByCluster(cluster);
    }

    /**
     * Updates an existing course by ID.
     *
     * <p>Only the {@code courseName} is updated. Cascades the name change to all associated sections.</p>
     *
     * @param id The unique ID of the course to update.
     * @param updatedCourse The updated details (only {@code courseName} is applied).
     * @return The updated {@link Courses} entity (with refreshed timestamps).
     *
     * @throws RuntimeException If the course is not found.
     */
    public Courses updateCourse(String id, Courses updatedCourse) {
        Courses existing = getCourseById(id);
        existing.setCourseName(updatedCourse.getCourseName());
        academicSectionService.updateSectionsForCourseNameChange(existing.getId(), existing.getCourseName());
        return courseRepository.save(existing);
    }

    /**
     * Deletes a course and all its referencing sections.
     *
     * <p>Cascades deletion: sections are deleted first, then the course.</p>
     *
     * @param id The unique ID of the course to delete.
     *
     * @throws RuntimeException If the course is not found (implicit via repo).
     */
    public void deleteCourse(String id) {
        academicSectionService.deleteSectionsByCourse(id);
        courseRepository.deleteById(id);
    }
}
