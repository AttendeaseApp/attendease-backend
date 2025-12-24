package com.attendease.backend.osa.controller.academic.cluster.management;

import com.attendease.backend.domain.clusters.Clusters;
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
@RequestMapping("/api/clusters")
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
     * @return The created {@link Clusters} entity (HTTP 200 OK).
     *
     * @throws IllegalArgumentException If the cluster name already exists.
     */
    @PostMapping
    public ResponseEntity<Clusters> createNewCluster(@RequestBody @Valid Clusters cluster) {
        return ResponseEntity.ok(clusterManagementService.createNewCluster(cluster));
    }

    /**
     * Retrieves all clusters.
     *
     * @return A list of all {@link Clusters} (HTTP 200 OK).
     */
    @GetMapping
    public ResponseEntity<List<Clusters>> getAllClusters() {
        return ResponseEntity.ok(clusterManagementService.getAllClusters());
    }

    /**
     * Retrieves a specific cluster by its ID.
     *
     * @param clusterId The unique ID of the cluster.
     * @return The {@link Clusters} entity (HTTP 200 OK).
     *
     * @throws RuntimeException If the cluster is not found.
     */
    @GetMapping("/{clusterId}")
    public ResponseEntity<Clusters> getClusterById(@PathVariable String clusterId) {
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
     * @return The updated {@link Clusters} entity (HTTP 200 OK).
     *
     * @throws RuntimeException If the cluster is not found.
     * @throws IllegalArgumentException If the new name already exists.
     */
    @PutMapping("/{clusterId}")
    public ResponseEntity<Clusters> updateClusterById(@PathVariable String clusterId, @RequestBody Clusters cluster) {
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
     *
     * @throws RuntimeException If the cluster is not found.
     */
    @DeleteMapping("/{clusterId}")
    public ResponseEntity<Void> deleteById(@PathVariable String clusterId) {
        clusterManagementService.deleteCluster(clusterId);
        return ResponseEntity.noContent().build();
    }
}
