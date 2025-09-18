package com.attendease.backend.repository.sections;

import com.attendease.backend.domain.students.Sections;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectionsRepository extends MongoRepository<Sections, String> {
    Optional<Sections> findByNameAndCourse(String name, String courseName);
    Optional<Sections> findById(String id);
    Optional<Sections> findByNameAndCourseId(String name, String courseId);

}

