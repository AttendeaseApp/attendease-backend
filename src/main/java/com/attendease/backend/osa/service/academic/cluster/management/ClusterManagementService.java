package com.attendease.backend.osa.service.academic.cluster.management;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.event.Event;

import java.util.List;

/**
 * {@code ManagementAcademicClusterService} is a service layer responsible for managing academic clusters (e.g., "CETE", "CBAM").
 *
 * <p>Provides CRUD operations for managing clusters</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-19
 */
public interface ClusterManagementService {

    /**
     * {@code createNewCluster} is used to create a new cluster.
     *
     * @param cluster The {@link Clusters} entity to create (must have a non-blank {@code clusterName}).
     * @return The saved {@link Clusters} entity (with auto-generated ID and timestamps).
     *
     * @throws IllegalArgumentException If a cluster with the same name already exists.
     */
    Clusters createNewCluster(Clusters cluster);

    /**
     * {@code getAllClusters} is used to retrieve all clusters.
     *
     * @return A {@link List} of all {@link Clusters} entities.
     */
    List<Clusters> getAllClusters();

    /**
     * {@code getClusterByClusterId} is used to retrieves a cluster by its ID.
     *
     * @param clusterId The unique ID of the cluster.
     * @return The {@link Clusters} entity if found.
     *
     * @throws RuntimeException If the cluster is not found.
     */
    Clusters getClusterByClusterId(String clusterId);

    /**
     * {@code updateCluster} is used to update an existing cluster's details.
     *
     * @param clusterId The unique ID of the cluster to update.
     * @param updatedCluster The updated details (only {@code clusterName} is applied).
     * @return The updated {@link Clusters} entity (with refreshed timestamps).
     *
     * @throws RuntimeException If the cluster is not found.
     */
    Clusters updateCluster(String clusterId, Clusters updatedCluster);

    /**
     * {@code deleteCLuster} is used to deletes a cluster only if no other dependencies exist including
     * {@link com.attendease.backend.domain.courses.Courses} and {@link Event}.
     *
     * @param clusterId The unique ID of the cluster to delete.
     *
     * @throws IllegalStateException If the cluster is not found or dependencies exist (with detailed message including counts).
     */
    void deleteCluster(String clusterId);
}
