package com.attendease.backend.repository.event;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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
     *
     * @param status the {@link EventStatus} to filter by
     * @return a list of {@link Event} with the specified status
     */
    List<Event> findByEventStatus(EventStatus status);

    /**
     * Finds all event sessions whose status is in the given list of statuses.
     *
     * @param statuses a list of {@link EventStatus} values to filter by
     * @return a list of {@link Event} matching any of the specified statuses
     */
    List<Event> findByEventStatusIn(List<EventStatus> statuses);

    /**
     * Retrieves all event sessions ordered by their creation time in descending order.
     *
     * @return a list of {@link Event} ordered from most recently created to oldest
     */
    List<Event> findAllByOrderByCreatedDesc();

    Long countByVenueLocationId(String venueLocationId);

    List<Event> findByVenueLocationId(String venueLocationId);

    Long countByEligibleStudentsClusterContaining(String clusterId);

    Long countByEligibleStudentsClusterNamesContaining(@NotBlank(message = "Cluster name is required") String clusterName);

    Long countByEligibleStudentsCourseContaining(String id);

    Long countByEligibleStudentsCourseNamesContaining(@NotBlank(message = "Course name is required") String courseName);

    Long countByEligibleStudentsSectionsContaining(String id);

    Long countByEligibleStudentsSectionNamesContaining(String name);
}
