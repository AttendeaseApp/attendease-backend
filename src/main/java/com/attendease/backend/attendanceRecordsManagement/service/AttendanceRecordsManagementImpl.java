package com.attendease.backend.attendanceRecordsManagement.service;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceRecordsManagementImpl {

    private final EventSessionsRepository eventSessionsRepository;

    public List<EventSessions> getEventsByStatus(EventStatus status) {
        return eventSessionsRepository.findByEventStatus(status);
    }


}
