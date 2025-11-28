package com.attendease.backend.repository.course;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Courses} entities.
 *
 * <p><strong>Collection:</strong> {@code courses} (as defined in {@link Courses} @Document).</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-19
 */
@Repository
public interface CourseRepository extends MongoRepository<Courses, String> {
    Optional<Courses> findByCourseName(String name);
    List<Courses> findByCluster(Clusters cluster);
    Optional<Courses> findByCourseNameAndCluster(String courseName, Clusters cluster);

    List<Courses> findByClusterClusterIdIn(List<String> clusterIds);

    List<Courses> findByClusterClusterId(String clusterId);
}
