package com.attendease.backend.repository.locations;

import com.attendease.backend.domain.locations.EventLocations;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link EventLocations} documents in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard operations such as {@code save}, {@code findAll},
 * {@code findById}, and {@code delete}. This repository currently does not define any custom query methods,
 * but can be extended in the future to include queries specific to event locations.
 * </p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Repository
public interface LocationRepository extends MongoRepository<EventLocations, String> {
    Optional<EventLocations> findByLocationName(String locationName);
}
