package com.attendease.backend.eventMonitoring.service;

import com.attendease.backend.eventMonitoring.dto.EventAttendanceDto;
import com.attendease.backend.eventMonitoring.repository.EventRepository;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.records.AttendanceRecords;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EventService {
    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventSessions> getOngoingEvents() {
        return eventRepository.findOngoingEvents();
    }

    public EventSessions getEventById(String eventId) {
        return eventRepository.findById(eventId);
    }

    public void monitorAttendance(String eventId, ConcurrentHashMap<String, WebSocketSession> sessions) {
        List<AttendanceRecords> records = eventRepository.getAttendanceRecords(eventId);
        List<EventAttendanceDto> attendanceDTOs = records.stream().map(record -> {
            EventAttendanceDto dto = new EventAttendanceDto();
            dto.setRecordId(record.getRecordId());
            dto.setStudentNumber(record.getStudentNumberRefId().getId());
            dto.setEventId(record.getEventRefId().getId());
            dto.setLocationId(record.getLocationRefId().getId());
            dto.setTimeIn(record.getTimeIn().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            dto.setTimeOut(record.getTimeOut() != null ? record.getTimeOut().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
            dto.setAttendanceStatus(record.getAttendanceStatus());
            return dto;
        }).toList();

        sessions.forEach((sessionId, session) -> {
            try {
                session.sendMessage(new TextMessage(attendanceDTOs.toString()));
            } catch (Exception e) {
                sessions.remove(sessionId);
            }
        });
    }
}
