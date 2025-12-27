package com.attendease.backend.repository.course;

import com.attendease.backend.domain.cluster.Cluster;
import com.attendease.backend.domain.course.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Course} entities.
 *
 * <p><strong>Collection:</strong> {@code courses} (as defined in {@link Course} @Document).</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-19
 */
@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    Optional<Course> findByCourseName(String name);
    List<Course> findByCluster(Cluster cluster);
    Optional<Course> findByCourseNameAndCluster(String courseName, Cluster cluster);

    List<Course> findByClusterClusterIdIn(List<String> clusterIds);

    List<Course> findByClusterClusterId(String clusterId);

    Long countByCluster(Cluster cluster);
}
