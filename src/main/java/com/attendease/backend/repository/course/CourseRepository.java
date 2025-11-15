package com.attendease.backend.repository.course;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Courses, String> {
    Optional<Courses> findByCourseName(String name);
    List<Courses> findByCluster(Clusters cluster);
}
