package com.attendease.backend.osaModule.service.management.academic.cluster;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.repository.clusters.ClustersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicClusterService {

    private final ClustersRepository clusterRepository;

    public Clusters createCluster(Clusters cluster) {
        if (clusterRepository.findByClusterName(cluster.getClusterName()).isPresent()) {
            throw new IllegalArgumentException("Cluster already exists.");
        }
        return clusterRepository.save(cluster);
    }

    public List<Clusters> getAllClusters() {
        return clusterRepository.findAll();
    }

    public Clusters getClusterById(String id) {
        return clusterRepository.findById(id).orElseThrow(() -> new RuntimeException("Cluster not found."));
    }

    public Clusters updateCluster(String id, Clusters updatedCluster) {
        Clusters existing = getClusterById(id);
        existing.setClusterName(updatedCluster.getClusterName());
        return clusterRepository.save(existing);
    }

    public void deleteCluster(String id) {
        clusterRepository.deleteById(id);
    }
}

