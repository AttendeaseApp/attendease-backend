package com.attendease.backend.osaModule.controller.management.academic.cluster;

import com.attendease.backend.osaModule.service.management.academic.cluster.AcademicClusterService;
import com.attendease.backend.domain.students.Clusters;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clusters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class AcademicClusterController {

    private final AcademicClusterService clusterService;

    @PostMapping
    public ResponseEntity<Clusters> create(@RequestBody @Valid Clusters cluster) {
        return ResponseEntity.ok(clusterService.createCluster(cluster));
    }

    @GetMapping
    public ResponseEntity<List<Clusters>> getAll() {
        return ResponseEntity.ok(clusterService.getAllClusters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clusters> getById(@PathVariable String id) {
        return ResponseEntity.ok(clusterService.getClusterById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Clusters> update(@PathVariable String id, @RequestBody Clusters cluster) {
        return ResponseEntity.ok(clusterService.updateCluster(id, cluster));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        clusterService.deleteCluster(id);
        return ResponseEntity.noContent().build();
    }
}

