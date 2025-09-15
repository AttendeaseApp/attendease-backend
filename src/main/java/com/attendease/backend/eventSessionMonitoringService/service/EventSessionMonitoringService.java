package com.attendease.backend.eventSessionMonitoringService.service;

import com.attendease.backend.model.enums.AttendanceStatus;
import com.attendease.backend.model.enums.EventStatus;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.records.AttendanceRecords;
import com.attendease.backend.model.records.AttendanceStatus.AttendanceStatusReport;
import com.attendease.backend.model.students.Students;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    //TODO
    public List<Students> getExpectedStudentsForEvent(String eventId) {
        EventSessions event = eventSessionsRepository.findById(eventId).orElseThrow();
        String eligible = event.getEligibleStudents();
        List<String> studentIds = Arrays.asList(eligible.split(","));
        return studentRepository.findByIdIn(studentIds);
    }


    public AttendanceStatusReport getAttendanceStatusReport(String eventId) {
        List<Students> expectedStudents = getExpectedStudentsForEvent(eventId);

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository.findByEvent_Id(eventId);

        Set<String> checkedInStudentIds = attendanceRecords.stream()
                .map(record -> record.getStudent().getId())
                .collect(Collectors.toSet());

        List<Students> checkedInStudents = expectedStudents.stream()
                .filter(student -> checkedInStudentIds.contains(student.getId()))
                .collect(Collectors.toList());

        List<Students> missingStudents = expectedStudents.stream()
                .filter(student -> !checkedInStudentIds.contains(student.getId()))
                .collect(Collectors.toList());

        return new AttendanceStatusReport(checkedInStudents, missingStudents);
    }



}

