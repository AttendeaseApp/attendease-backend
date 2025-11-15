package com.attendease.backend.repository.clusters;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.attendease.backend.domain.clusters.Clusters;

import java.util.Optional;

@Repository
public interface ClustersRepository extends MongoRepository<Clusters, String> {
    Optional<Clusters> findByClusterName(String name);
}
