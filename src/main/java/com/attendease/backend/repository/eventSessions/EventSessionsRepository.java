package com.attendease.backend.repository.eventSessions;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import java.util.Date;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on {@link EventSessions} documents in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard operations such as {@code save}, {@code findAll},
 * {@code findById}, and {@code delete}. This repository also defines custom query methods to
 * retrieve event sessions by status, date ranges, or combinations of both, and to order events by creation time.
 * </p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Repository
public interface EventSessionsRepository extends MongoRepository<EventSessions, String> {
    /**
     * Finds all event sessions with the given status.
     *
     * @param status the {@link EventStatus} to filter by
     * @return a list of {@link EventSessions} with the specified status
     */
    List<EventSessions> findByEventStatus(EventStatus status);

    /**
     * Finds all event sessions whose status is in the given list of statuses.
     *
     * @param statuses a list of {@link EventStatus} values to filter by
     * @return a list of {@link EventSessions} matching any of the specified statuses
     */
    List<EventSessions> findByEventStatusIn(List<EventStatus> statuses);

    /**
     * Finds all event sessions that fall within the specified date range.
     *
     * @param from the start date of the range (inclusive)
     * @param to the end date of the range (inclusive)
     * @return a list of {@link EventSessions} occurring within the specified date range
     */
    @Query("{ 'startDateTime': { $gte: ?0 }, 'endDateTime': { $lte: ?1 } }")
    List<EventSessions> findByDateRange(Date from, Date to);

    /**
     * Finds all event sessions with the given status that also fall within the specified date range.
     *
     * @param status the {@link EventStatus} to filter by
     * @param from the start date of the range (inclusive)
     * @param to the end date of the range (inclusive)
     * @return a list of {@link EventSessions} matching the status and date range criteria
     */
    @Query("{ 'eventStatus': ?0, 'startDateTime': { $gte: ?1 }, 'endDateTime': { $lte: ?2 } }")
    List<EventSessions> findByStatusAndDateRange(EventStatus status, Date from, Date to);

    /**
     * Retrieves all event sessions ordered by their creation time in descending order.
     *
     * @return a list of {@link EventSessions} ordered from most recently created to oldest
     */
    List<EventSessions> findAllByOrderByCreatedAtDesc();

    Long countByEventLocationId(String eventLocationId);
}
