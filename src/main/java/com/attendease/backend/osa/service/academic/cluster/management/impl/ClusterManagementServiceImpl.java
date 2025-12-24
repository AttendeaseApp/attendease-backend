package com.attendease.backend.osa.service.academic.cluster.management.impl;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.osa.service.academic.cluster.management.ClusterManagementService;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public final class ClusterManagementServiceImpl implements ClusterManagementService {

    private final ClustersRepository clusterRepository;
    private final CourseRepository courseRepository;
    private final EventRepository eventRepository;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public Clusters createNewCluster(Clusters cluster) {
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

    @Override
    public List<Clusters> getAllClusters() {
        return clusterRepository.findAll();
    }

    @Override
    public Clusters getClusterByClusterId(String clusterId) {
        return clusterRepository.findById(clusterId).orElseThrow(() -> new RuntimeException("Cluster not found with ID: " + clusterId));
    }

    @Override
    @Transactional
    public Clusters updateCluster(String clusterId, Clusters updatedCluster) {
        Clusters existing = getClusterByClusterId(clusterId);
        String newClusterName = updatedCluster.getClusterName().trim();

        if (newClusterName.isEmpty()) {
            throw new IllegalArgumentException("Cluster name cannot be empty.");
        }

        userValidator.validateClusterNameFormat(newClusterName);

        clusterRepository.findByClusterName(newClusterName).filter(c -> !c.getClusterId().equals(clusterId)).ifPresent(c -> {
                    throw new IllegalArgumentException("Cluster name '" + newClusterName + "' is already in use. Please choose a unique name.");});

        existing.setClusterName(newClusterName);
        return clusterRepository.save(existing);
    }

    @Override
    @Transactional
    public final void deleteCluster(String clusterId) {
        Clusters cluster = getClusterByClusterId(clusterId);
        long courseCount = courseRepository.countByCluster(cluster);
        long eventCountById = eventRepository.countByEligibleStudentsClustersContaining(cluster.getClusterId());
        long eventCountByName = eventRepository.countByEligibleStudentsClusterNamesContaining(cluster.getClusterName());
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
        clusterRepository.deleteById(clusterId);
    }
}
