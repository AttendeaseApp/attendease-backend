package com.attendease.backend.repository.locations;

import com.attendease.backend.model.locations.EventLocations;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationRepository extends MongoRepository<EventLocations, String> {

}
