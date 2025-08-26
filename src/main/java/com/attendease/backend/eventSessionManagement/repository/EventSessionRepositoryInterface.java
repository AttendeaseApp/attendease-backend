package com.attendease.backend.eventSessionManagement.repository;

import com.attendease.backend.model.enums.EventStatus;
import com.attendease.backend.model.events.EventSessions;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface EventSessionRepositoryInterface {

    String save(EventSessions eventSession) throws ExecutionException, InterruptedException;

    Optional<EventSessions> findById(String eventId) throws ExecutionException, InterruptedException;

    List<EventSessions> findAll() throws ExecutionException, InterruptedException;

    List<EventSessions> findByStatus(EventStatus status) throws ExecutionException, InterruptedException;

    List<EventSessions> findByDateRange(Date dateFrom, Date dateTo) throws ExecutionException, InterruptedException;

    List<EventSessions> findByStatusAndDateRange(EventStatus status, Date dateFrom, Date dateTo) throws ExecutionException, InterruptedException;

    void deleteById(String eventId) throws ExecutionException, InterruptedException;

    boolean existsById(String eventId) throws ExecutionException, InterruptedException;
}
