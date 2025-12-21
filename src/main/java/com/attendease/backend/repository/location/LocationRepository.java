package com.attendease.backend.repository.location;

import com.attendease.backend.domain.enums.location.LocationEnvironment;
import com.attendease.backend.domain.enums.location.LocationPurpose;
import com.attendease.backend.domain.location.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link Location} documents in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard operations such as {@code save}, {@code findAll},
 * {@code findById}, and {@code delete}. This repository currently does not define any custom query methods,
 * but can be extended in the future to include queries specific to event locations.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-15
 */
@Repository
public interface LocationRepository extends MongoRepository<Location, String> {

    Optional<Location> findByLocationNameAndEnvironmentAndPurpose(String trimmedName, LocationEnvironment environment, LocationPurpose purpose);
}
