/**
 * WebSocket Controller for Automated Attendance Tracking.
 *
 * <b>Endpoint:</b> <code>ws/checkout/{studentNumber}/ongoing/attendance/monitorLocation</code>
 * <br>
 * <b>How to use:</b>
 * <ul>
 *   <li>Connect via WebSocket client</li>
 *   <li>Send JSON payloads representing location updates:</li>
 * </ul>
 * <pre>
 * {
 *   "studentNumber": "CT00-0000",
 *   "eventId": "EVENT123",
 *   "latitude": 14.1498,
 *   "longitude": 120.9555,
 *   "presentAtLocation": true,
 *   "lastExitTime": "2025-09-01T10:30:00Z",
 *   "lastReturnTime": "2025-09-01T10:40:00Z"
 * }
 * </pre>
 * <ul>
 *   <li>Server will respond with attendance status or monitoring updates.</li>
 * </ul>
 */
package com.attendease.backend.attendanceTrackingService.controller;

import com.attendease.backend.attendanceTrackingService.dto.LocationInfoDto;
import com.attendease.backend.attendanceTrackingService.dto.LocationMonitorDto;
import com.attendease.backend.attendanceTrackingService.service.attendanceTrackingService.AutomatedAttendanceTrackingService;
import com.attendease.backend.model.enums.AttendanceStatus;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.records.AttendanceRecords;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AttendanceTrackingWebSocketController extends TextWebSocketHandler {
    private final AutomatedAttendanceTrackingService trackingService;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Autowired
    public AttendanceTrackingWebSocketController(AutomatedAttendanceTrackingService trackingService) {
        this.trackingService = trackingService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        session.sendMessage(new TextMessage("Connected to Automated Attendance Tracking Service."));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            LocationMonitorDto dto = objectMapper.readValue(message.getPayload(), LocationMonitorDto.class);

            LocationInfoDto info = new LocationInfoDto(
                    dto.isPresentAtLocation(),
                    dto.getLastExitTime() != null ? Timestamp.valueOf(dto.getLastExitTime()) : null,
                    dto.getLastReturnTime() != null ? Timestamp.valueOf(dto.getLastReturnTime()) : null
            );

            trackingService.updateLocation(dto.getStudentNumber(), info);

            EventSessions mockEvent = new EventSessions();
            mockEvent.setStartDateTime(new Date(System.currentTimeMillis() - 30 * 60 * 1000));
            mockEvent.setEndDateTime(new Date(System.currentTimeMillis() + 30 * 60 * 1000));

            AttendanceRecords mockRecord = new AttendanceRecords();
            mockRecord.setTimeIn(new Date(System.currentTimeMillis() - 25 * 60 * 1000));

            AttendanceStatus status = trackingService.evaluateAttendance(mockEvent, mockRecord, info);

            String response = (status != null) ? "Attendance status: " + status.name() : "Monitoring: not enough data to determine status.";

            session.sendMessage(new TextMessage(response));

        } catch (Exception e) {
            session.sendMessage(new TextMessage("Invalid payload: " + e.getMessage()));
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
    }
}
