package com.attendease.backend.repository.event;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on {@link Event} documents in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard operations such as {@code save}, {@code findAll},
 * {@code findById}, and {@code delete}. This repository also defines custom query methods to
 * retrieve event sessions by status, date ranges, or combinations of both, and to order events by creation time.
 * </p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    /**
     * Finds all event sessions with the given status.
     */
    List<Event> findByEventStatus(EventStatus status);

    /**
     * Finds all event sessions whose status is in the given list of statuses.
     */
    List<Event> findByEventStatusIn(List<EventStatus> statuses);

    /**
     * Retrieves all event sessions ordered by their creation time in descending order.
     */
    List<Event> findAllByOrderByCreatedDesc();

    /**
     * Counts location by venue location id.
     */
    Long countByVenueLocationId(String venueLocationId);

    /**
     * Retrieves location by using venue location id.
     */
    List<Event> findByVenueLocationId(String venueLocationId);

    /**
     * Counts the number of events where the specified cluster ID is in the eligible students list.
     */
    Long countByEligibleStudentsClustersContaining(String clusterId);

    /**
     * Counts the number of events where the specified cluster name is in the eligible students list.
     */
    Long countByEligibleStudentsClusterNamesContaining(String clusterName);

    /**
     * Counts the number of events where the specified course ID is in the eligible students list.
     */
    Long countByEligibleStudentsCoursesContaining(String id);

    /**
     * Counts the number of events where the specified course name is in the eligible students list.
     */
    Long countByEligibleStudentsCourseNamesContaining(String courseName);

    /**
     * Counts the number of events where the specified section ID is in the eligible students list.
     */
    Long countByEligibleStudentsSectionsContaining(String id);

    /**
     * Counts the number of events where the specified section name is in the eligible students list.
     */
    Long countByEligibleStudentsSectionNamesContaining(String name);

    /**
     * Finds events at a specific venue location that do not match any of the excluded statuses.
     * Typically used for checking scheduling conflicts.
     */
    List<Event> findByVenueLocationIdAndEventStatusNotIn(String venueLocationId, List<EventStatus> excludedConflictStatuses);

    /**
     * Finds events at a specific registration location that do not match any of the excluded statuses.
     * Useful for detecting location-based conflicts during the registration phase.
     */
    List<Event> findByRegistrationLocationIdAndEventStatusNotIn(String registrationLocationId, List<EventStatus> excludedConflictStatuses);
}
