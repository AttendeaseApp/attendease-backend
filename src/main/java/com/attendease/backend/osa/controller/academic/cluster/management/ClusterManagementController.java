package com.attendease.backend.osa.controller.academic.cluster.management;

import com.attendease.backend.domain.cluster.Cluster;
import com.attendease.backend.osa.service.academic.cluster.management.ClusterManagementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * {@code ManagementAcademicClusterController} is used for managing cluster courses.
 *
 * <p>This controller provides CRUD operations for clusters, ensuring that all endpoints are secured
 * for osa (Office of Student Affairs) role user only.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-19
 */
@RestController
@RequestMapping("/api/osa/cluster/management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class ClusterManagementController {

    private final ClusterManagementService clusterManagementService;

    /**
     * Creates a new cluster.
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>{@code
     * {
     *   "clusterName": "CETE"
     * }
     * }</pre>
     *
     * @param cluster The cluster details (validated; must include a non-blank {@code clusterName}).
     * @return The created {@link Cluster} entity (HTTP 200 OK).
     *
     * @throws IllegalArgumentException If the cluster name already exists.
     */
    @PostMapping
    public ResponseEntity<Cluster> addNewCluster(@RequestBody @Valid Cluster cluster) {
        return ResponseEntity.ok(clusterManagementService.addNewCluster(cluster));
    }

    /**
     * Retrieves all clusters.
     *
     * @return A list of all {@link Cluster} (HTTP 200 OK).
     */
    @GetMapping
    public ResponseEntity<List<Cluster>> getAllClusters() {
        return ResponseEntity.ok(clusterManagementService.getAllClusters());
    }

    /**
     * Retrieves a specific cluster by its ID.
     *
     * @param clusterId The unique ID of the cluster.
     * @return The {@link Cluster} entity (HTTP 200 OK).
     */
    @GetMapping("/{clusterId}")
    public ResponseEntity<Cluster> getClusterById(@PathVariable String clusterId) {
        return ResponseEntity.ok(clusterManagementService.getClusterByClusterId(clusterId));
    }

    /**
     * Updates an existing cluster by ID.
     *
     * <p>Only the {@code clusterName} is updated. No cascade to courses (references use ID).</p>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>{@code
     * {
     *   "clusterName": "ENGINEERING"
     * }
     * }</pre>
     *
     * @param clusterId The unique ID of the cluster to update.
     * @param cluster The updated cluster details (only {@code clusterName} is applied).
     * @return The updated {@link Cluster} entity (HTTP 200 OK).
     */
    @PutMapping("/{clusterId}")
    public ResponseEntity<Cluster> updateClusterById(@PathVariable String clusterId, @RequestBody Cluster cluster) {
        return ResponseEntity.ok(clusterManagementService.updateCluster(clusterId, cluster));
    }

    /**
     * Deletes a cluster and all its associated courses and sections.
     *
     * <p>This performs a full cascading delete: courses under the cluster are deleted first
     * (including their sections), then the cluster.</p>
     *
     * @param clusterId The unique ID of the cluster to delete.
     * @return No content (HTTP 204 No Content).
     */
    @DeleteMapping("/{clusterId}")
    public ResponseEntity<Void> deleteById(@PathVariable String clusterId) {
        clusterManagementService.deleteCluster(clusterId);
        return ResponseEntity.noContent().build();
    }
}
