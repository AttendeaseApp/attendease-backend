package com.attendease.backend.repository.sections;

import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.sections.Sections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Sections} entities.
 *
 * <p>This repository provides custom query methods for retrieving sections by name, course,
 * or combinations thereof. It extends {@link MongoRepository} for basic CRUD operations
 * (e.g., {@code save}, {@code findById}, {@code deleteById}). All methods are automatically
 * implemented by Spring Data MongoDB based on naming conventions.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-19
 */
@Repository
public interface SectionsRepository extends MongoRepository<Sections, String> {
    /**
     * Finds a section by its full name and the course name (e.g., name="BSIT-101", courseName="BSIT").
     *
     * @param name The full section name (e.g., "BSIT-101").
     * @param courseName The name of the parent course (e.g., "BSIT").
     * @return An {@link Optional} containing the matching {@link Sections} if found.
     */
    Optional<Sections> findByNameAndCourse(String name, String courseName);

    /**
     * Finds a section by its full name and the course ID.
     *
     * @param name The full section name (e.g., "BSIT-101").
     * @param courseId The ID of the parent course.
     * @return An {@link Optional} containing the matching {@link Sections} if found.
     */
    Optional<Sections> findByNameAndCourseId(String name, String courseId);

    /**
     * Finds a section by the parent {@link Courses} entity and section name.
     *
     * @param course The parent {@link Courses} entity (loaded via @DBRef).
     * @param name The full section name (e.g., "BSIT-101").
     * @return An {@link Optional} containing the matching {@link Sections} if found.
     */
    Optional<Sections> findByCourseAndName(Courses course, String name);

    /**
     * Finds a section by its full name globally.
     *
     * <p>Assumes section names are unique across the system (e.g., no two "BSIT-101" from different courses).</p>
     *
     * @param name The full section name (e.g., "BSIT-101").
     * @return An {@link Optional} containing the matching {@link Sections} if found.
     */
    Optional<Sections> findByName(String name);

    /**
     * Finds all sections associated with a specific {@link Courses} entity.
     *
     * <p>Used for retrieving all sections under a course (e.g., for listing or bulk deletion).</p>
     *
     * @param course The parent {@link Courses} entity (loaded via @DBRef).
     * @return A {@link List} of all matching {@link Sections}.
     */
    List<Sections> findByCourse(Courses course);

    List<Sections> findByCourseIdIn(List<String> course);

    List<Sections> findByCourseId(String id);

    Long countByCourse(Courses course);
}
