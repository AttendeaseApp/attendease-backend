package com.attendease.backend.osaModule.service.management.academic.cluster;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.osaModule.service.management.academic.course.AcademicCourseService;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import java.util.List;

import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
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
    private final EventSessionsRepository eventSessionsRepository;
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
     * Deletes a cluster by its ID **only if no dependencies exist**.
     *
     * <p>Prevents deletion if courses or event sessions reference the cluster. Counts dependencies
     * and throws a detailed exception with counts and rationale (similar to attendance checks in event deletion).</p>
     *
     * @param id The unique ID of the cluster to delete.
     *
     * @throws IllegalStateException If the cluster is not found or dependencies exist (with detailed message including counts).
     */
    public void deleteCluster(String id) {
        Clusters cluster = getClusterById(id);
        Long courseCount = courseRepository.countByCluster(cluster);
        Long eventCountById = eventSessionsRepository.countByEligibleStudentsClusterContaining(cluster.getClusterId());
        Long eventCountByName = eventSessionsRepository.countByEligibleStudentsClusterNamesContaining(cluster.getClusterName());
        Long totalEventCount = eventCountById + eventCountByName;

        if (courseCount > 0 || eventCountById > 0 || eventCountByName > 0) {
            String clusterName = cluster.getClusterName();
            StringBuilder message = new StringBuilder("You cannot delete cluster '" + clusterName + "' due to existing dependencies (").append(courseCount).append(" courses");
            if (eventCountById > 0 || eventCountByName > 0) {
                message.append(", ").append(totalEventCount).append(" event sessions (").append(eventCountById).append(" by ID, ").append(eventCountByName).append(" by name; possible overlap)").append(")");
            } else {
                message.append(")");
            }
            message.append(". This action is prevented to protect data integrity and avoid orphaned references. ").append("Reassign or remove dependencies first (e.g., update courses or event eligibility criteria).");
            throw new IllegalStateException(message.toString());
        }
        clusterRepository.deleteById(id);
    }
}
