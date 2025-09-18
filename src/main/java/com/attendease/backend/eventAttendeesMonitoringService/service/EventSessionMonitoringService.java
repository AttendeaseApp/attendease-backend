package com.attendease.backend.eventAttendeesMonitoringService.service;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EventSessionMonitoringService {

    private final EventSessionsRepository eventSessionsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final StudentRepository studentRepository;

    public List<EventSessions> getOngoingEvents() {
        return eventSessionsRepository.findByEventStatusIn(Arrays.asList(EventStatus.ONGOING, EventStatus.ACTIVE));
    }

    public List<EventSessions> getEndedEvents() {
        return eventSessionsRepository.findByEndDateTimeBeforeAndEventStatus(new Date(), EventStatus.ONGOING);
    }

    public List<EventSessions> getAllSortedByCreatedAt() {
        return eventSessionsRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<EventSessions> findById(String id) {
        return eventSessionsRepository.findById(id);
    }

    public List<Students> getAttendeesByEvent(String eventId) {
        List<AttendanceRecords> records = attendanceRecordsRepository.findByEvent_Id(eventId);
        return records.stream()
                .map(AttendanceRecords::getStudent)
                .distinct()
                .toList();
    }
}

