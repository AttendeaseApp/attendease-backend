package com.attendease.backend.eventMonitoring.repository;

import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.records.AttendanceRecords;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Abstraction for event monitoring repository operations.
 */
public interface EventRepositoryInterface {
    List<EventSessions> findOngoingEvents();
    List<EventSessions> findAll() throws ExecutionException, InterruptedException;
    EventSessions findById(String eventId);
    void saveAttendanceRecord(AttendanceRecords record);
    List<AttendanceRecords> getAttendanceRecords(String eventId);
}
