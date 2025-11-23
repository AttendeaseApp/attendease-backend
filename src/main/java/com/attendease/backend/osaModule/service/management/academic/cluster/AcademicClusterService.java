package com.attendease.backend.osaModule.service.management.academic.cluster;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.osaModule.service.management.academic.course.AcademicCourseService;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@code AcademicClusterService} is a service layer for managing academic clusters (e.g., clusters like "CETE" and "CBAM").
 *
 * <p>This service provides CRUD operations for clusters, including uniqueness validation
 * on cluster names. It integrates with {@link ClustersRepository} for data access.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-19
 */
@Service
@RequiredArgsConstructor
public class AcademicClusterService {

    private final ClustersRepository clusterRepository;
    private final CourseRepository courseRepository;
    private final AcademicCourseService academicCourseService;

    /**
     * Creates a new cluster.
     *
     * <p>Validates uniqueness of the cluster name before saving. If duplicate, throws an exception.</p>
     *
     * @param cluster The {@link Clusters} entity to create (must have a non-blank {@code clusterName}).
     * @return The saved {@link Clusters} entity (with auto-generated ID and timestamps).
     *
     * @throws IllegalArgumentException If a cluster with the same name already exists.
     */
    public Clusters createCluster(Clusters cluster) {
        if (clusterRepository.findByClusterName(cluster.getClusterName()).isPresent()) {
            throw new IllegalArgumentException("Cluster already exists.");
        }
        return clusterRepository.save(cluster);
    }

    /**
     * Retrieves all clusters.
     *
     * @return A {@link List} of all {@link Clusters} entities.
     */
    public List<Clusters> getAllClusters() {
        return clusterRepository.findAll();
    }

    /**
     * Retrieves a cluster by its ID.
     *
     * @param id The unique ID of the cluster.
     * @return The {@link Clusters} entity if found.
     *
     * @throws RuntimeException If the cluster is not found.
     */
    public Clusters getClusterById(String id) {
        return clusterRepository.findById(id).orElseThrow(() -> new RuntimeException("Cluster not found."));
    }

    /**
     * Updates an existing cluster by ID.
     *
     * @param id The unique ID of the cluster to update.
     * @param updatedCluster The updated details (only {@code clusterName} is applied).
     * @return The updated {@link Clusters} entity (with refreshed timestamps).
     *
     * @throws RuntimeException If the cluster is not found.
     */
    public Clusters updateCluster(String id, Clusters updatedCluster) {
        Clusters existing = getClusterById(id);
        existing.setClusterName(updatedCluster.getClusterName());
        return clusterRepository.save(existing);
    }

    /**
     * Deletes a cluster by its ID, cascading to all child courses and their sections.
     *
     * <p>Full cascade: Fetches all courses under the cluster, deletes each course (which cascades
     * to its sections via {@link AcademicCourseService#deleteCourse(String)}), then deletes the cluster.
     * This ensures no orphaned references or data inconsistencies.</p>
     *
     * @param id The unique ID of the cluster to delete.
     *
     * @throws RuntimeException If the cluster is not found.
     */
    public void deleteCluster(String id) {
        Clusters cluster = getClusterById(id);
        List<Courses> courses = courseRepository.findByCluster(cluster);
        for (Courses course : courses) {
            academicCourseService.deleteCourse(course.getId());
        }
        clusterRepository.deleteById(id);
    }
}
