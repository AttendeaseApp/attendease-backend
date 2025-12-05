package com.attendease.backend.osaModule.service.management.academic.course;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.osaModule.service.management.academic.section.AcademicSectionService;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import java.util.List;

import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.validation.UserValidator;
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
    private final SectionsRepository sectionsRepository;
    private final StudentRepository studentRepository;
    private final EventSessionsRepository eventSessionsRepository;
    private final UserValidator userValidator;

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

        String courseName = course.getCourseName().trim();
        if (courseName.isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }

        userValidator.validateCourseNameFormat(courseName);

        if (courseRepository.findByCourseName(courseName).isPresent()) {
            throw new IllegalArgumentException("Course name '" + courseName + "' already exists.");
        }

        course.setCourseName(courseName);
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
        String newCourseName = updatedCourse.getCourseName().trim();

        if (newCourseName.isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }

        userValidator.validateCourseNameFormat(newCourseName);

        courseRepository.findByCourseName(newCourseName).filter(c -> !c.getId().equals(id)).ifPresent(c -> {
                    throw new IllegalArgumentException("Course name '" + newCourseName + "' already exists.");});

        existing.setCourseName(newCourseName);
        academicSectionService.updateSectionsForCourseNameChange(existing.getId(), newCourseName);
        return courseRepository.save(existing);
    }

    /**
     * Deletes a course by its ID, cascading to sections **only if sections have no dependencies**.
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
    public void deleteCourse(String id) {
        Courses course = getCourseById(id);
        long eventCountById = eventSessionsRepository.countByEligibleStudentsCourseContaining(course.getId());
        long eventCountByName = eventSessionsRepository.countByEligibleStudentsCourseNamesContaining(course.getCourseName());
        long totalEventCount = eventCountById + eventCountByName;

        long totalStudentCount = 0;
        long sectionCount = sectionsRepository.countByCourse(course);
        if (sectionCount > 0) {
            List<Sections> sections = sectionsRepository.findByCourse(course);
            for (Sections section : sections) {
                totalStudentCount += studentRepository.countBySection(section);
            }
        }

        if (eventCountById > 0 || eventCountByName > 0) {
            String courseName = course.getCourseName();
            String message = "Cannot delete course '" + courseName +
                    "' as it is in use by " + totalEventCount + " event sessions (" +
                    eventCountById + " by ID, " + eventCountByName + " by name; possible overlap)" +
                    ") across " + sectionCount + " sections, potentially impacting " +
                    totalStudentCount + " enrolled students overall (via section eligibility). " +
                    "These events may restrict access for those students. To proceed, update event eligibility criteria first. This prevents data inconsistencies.";
            throw new IllegalStateException(message);
        }
        if (sectionCount > 0) {
            List<Sections> sections = sectionsRepository.findByCourse(course);
            for (Sections section : sections) {
                academicSectionService.deleteSection(section.getId());
            }
        }
        courseRepository.deleteById(id);
    }
}
