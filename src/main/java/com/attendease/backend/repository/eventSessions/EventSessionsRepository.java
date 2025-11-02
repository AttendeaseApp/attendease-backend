package com.attendease.backend.repository.eventSessions;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface EventSessionsRepository extends MongoRepository<EventSessions, String> {
    List<EventSessions> findByEventStatus(EventStatus status);

    List<EventSessions> findByEventStatusIn(List<EventStatus> statuses);

    @Query("{ 'startDateTime': { $gte: ?0 }, 'endDateTime': { $lte: ?1 } }")
    List<EventSessions> findByDateRange(Date from, Date to);

    @Query("{ 'eventStatus': ?0, 'startDateTime': { $gte: ?1 }, 'endDateTime': { $lte: ?2 } }")
    List<EventSessions> findByStatusAndDateRange(EventStatus status, Date from, Date to);

    List<EventSessions> findByEndDateTimeBeforeAndEventStatus(Date date, EventStatus status);

    List<EventSessions> findAllByOrderByCreatedAtDesc();

}
