package com.attendease.backend.osa.service.management.academic.cluster;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@code AcademicClusterService} is a service layer for managing academic clusters (e.g., "CETE", "CBAM").
 *
 * <p>Provides CRUD operations with strict validation:
 * - Cluster names must be unique and non-blank.
 * - Prevents deletion if courses or event sessions reference the cluster.
 * - Descriptive error messages for invalid operations.</p>
 *
 * @author ...
 * @since 2025-12-04
 */
@Service
@RequiredArgsConstructor
public class AcademicClusterService {

    private final ClustersRepository clusterRepository;
    private final CourseRepository courseRepository;
    private final EventSessionsRepository eventSessionsRepository;
    private final UserValidator userValidator;

    /**
     * Creates a new cluster with validation.
     *
     * @param cluster The {@link Clusters} entity to create (must have a non-blank {@code clusterName}).
     * @return The saved {@link Clusters} entity (with auto-generated ID and timestamps).
     *
     * @throws IllegalArgumentException If a cluster with the same name already exists.
     */
    public Clusters createCluster(Clusters cluster) {
        String name = cluster.getClusterName().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Cluster name cannot be empty.");
        }

        userValidator.validateClusterNameFormat(name);

        if (clusterRepository.findByClusterName(name).isPresent()) {
            throw new IllegalArgumentException("Cluster name '" + name + "' already exists. Please choose a unique name.");
        }

        cluster.setClusterName(name);
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
        return clusterRepository.findById(id).orElseThrow(() -> new RuntimeException("Cluster not found with ID: " + id));
    }

    /**
     * Updates an existing cluster.
     *
     * @param id The unique ID of the cluster to update.
     * @param updatedCluster The updated details (only {@code clusterName} is applied).
     * @return The updated {@link Clusters} entity (with refreshed timestamps).
     *
     * @throws RuntimeException If the cluster is not found.
     */
    public Clusters updateCluster(String id, Clusters updatedCluster) {
        Clusters existing = getClusterById(id);
        String newClusterName = updatedCluster.getClusterName().trim();

        if (newClusterName.isEmpty()) {
            throw new IllegalArgumentException("Cluster name cannot be empty.");
        }

        userValidator.validateClusterNameFormat(newClusterName);

        clusterRepository.findByClusterName(newClusterName).filter(c -> !c.getClusterId().equals(id)).ifPresent(c -> {
                    throw new IllegalArgumentException("Cluster name '" + newClusterName + "' is already in use. Please choose a unique name.");});

        existing.setClusterName(newClusterName);
        return clusterRepository.save(existing);
    }

    /**
     * Deletes a cluster only if no dependencies exist.
     *
     * @param id The unique ID of the cluster to delete.
     *
     * @throws IllegalStateException If the cluster is not found or dependencies exist (with detailed message including counts).
     */
    public void deleteCluster(String id) {
        Clusters cluster = getClusterById(id);
        long courseCount = courseRepository.countByCluster(cluster);
        long eventCountById = eventSessionsRepository.countByEligibleStudentsClusterContaining(cluster.getClusterId());
        long eventCountByName = eventSessionsRepository.countByEligibleStudentsClusterNamesContaining(cluster.getClusterName());
        long totalEventCount = eventCountById + eventCountByName;

        if (courseCount > 0 || totalEventCount > 0) {
            StringBuilder message = new StringBuilder(
                    "Cannot delete cluster '" + cluster.getClusterName() + "' due to existing dependencies: "
            ).append(courseCount).append(" course(s)");

            if (totalEventCount > 0) {
                message.append(", ").append(totalEventCount)
                        .append(" event session(s) (")
                        .append(eventCountById).append(" by ID, ")
                        .append(eventCountByName).append(" by name; possible overlap)")
                        .append(")");
            }
            message.append(". Remove dependencies first if you wish to proceed.");
            throw new IllegalStateException(message.toString());
        }
        clusterRepository.deleteById(id);
    }
}
