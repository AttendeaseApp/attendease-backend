package com.attendease.backend.repository.clusters;

import com.attendease.backend.domain.clusters.Clusters;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Clusters} entities.
 *
 * <p>This repository provides custom query methods for retrieving clusters by name.
 * It extends {@link MongoRepository} for basic CRUD operations (e.g., {@code save},
 * {@code findById}, {@code deleteById}).</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-19
 */
@Repository
public interface ClustersRepository extends MongoRepository<Clusters, String> {
    /**
     * Finds a cluster by its name globally.
     *
     * <p>Enforces uniqueness; used for lookups during course creation to associate courses with clusters.</p>
     *
     * @param name The cluster name (e.g., "CETE").
     * @return An {@link Optional} containing the matching {@link Clusters} if found.
     *
     * @throws IllegalArgumentException Implicitly if duplicate names exist (due to unique index).
     */
    Optional<Clusters> findByClusterName(String name);
}
