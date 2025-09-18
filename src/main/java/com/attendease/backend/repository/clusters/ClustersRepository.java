package com.attendease.backend.repository.clusters;

import com.attendease.backend.domain.students.Clusters;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClustersRepository extends MongoRepository<Clusters, String> {
    Optional<Clusters> findByClusterName(String name);
}
