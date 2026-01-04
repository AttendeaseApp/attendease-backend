package com.attendease.backend.osa.service.academic.cluster.management;

import com.attendease.backend.domain.cluster.Cluster;
import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.event.Event;

import java.util.List;

/**
 * {@code ClusterManagementService} is a service layer responsible for managing academic clusters (e.g., "CETE", "CBAM").
 *
 * <p>Provides CRUD operations for managing clusters</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-19
 */
public interface ClusterManagementService {

    /**
     * {@code addNewCluster} is used to create a new cluster.
     *
     * @param cluster The {@link Cluster} entity to create (must have a non-blank {@code clusterName}).
     * @return The saved {@link Cluster} entity (with auto-generated ID and timestamps).
     */
    Cluster addNewCluster(Cluster cluster);

    /**
     * {@code getAllClusters} is used to retrieve all clusters.
     *
     * @return A {@link List} of all {@link Cluster} entities.
     */
    List<Cluster> getAllClusters();

    /**
     * {@code getClusterByClusterId} is used to retrieves a cluster by its ID.
     *
     * @param clusterId The unique ID of the cluster.
     * @return The {@link Cluster} entity if found.
     */
    Cluster getClusterByClusterId(String clusterId);

    /**
     * {@code updateCluster} is used to update an existing cluster's details.
     *
     * @param clusterId The unique ID of the cluster to update.
     * @param updatedCluster The updated details (only {@code clusterName} is applied).
     * @return The updated {@link Cluster} entity (with refreshed timestamps).
     */
    Cluster updateCluster(String clusterId, Cluster updatedCluster);

    /**
     * {@code deleteCLuster} is used to deletes a cluster only if no other dependencies exist including
     * {@link Course} and {@link Event}.
     *
     * @param clusterId The unique ID of the cluster to delete.
     */
    void deleteCluster(String clusterId);
}
