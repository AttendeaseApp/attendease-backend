/**
 * WebSocket Handler for Event Attendance Monitoring.
 *
 * <b>Endpoint:</b> <code>/ws/events/{eventId}/monitoring</code>
 *
 * <b>How to use:</b>
 * <ul>
 *   <li>Connect via WebSocket client (e.g., Postman, browser, etc.)</li>
 *   <li>Send messages (e.g., "REFRESH") to request attendance updates.</li>
 *   <li>Receive attendance status and updates in real-time.</li>
 * </ul>
 * <b>Responses:</b> Attendance status and updates as JSON or text messages.
 */
package com.attendease.backend.eventMonitoring.handler;

import com.attendease.backend.eventMonitoring.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AttendanceWebSocketHandler implements WebSocketHandler {

    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionEventMap = new ConcurrentHashMap<>();

    public AttendanceWebSocketHandler(EventService eventService, ObjectMapper objectMapper) {
        this.eventService = eventService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String eventId = extractEventId(session.getUri());
        if (eventId != null) {
            sessions.put(session.getId(), session);
            sessionEventMap.put(session.getId(), eventId);
            log.info("WebSocket connection established for event: {} with session: {}", eventId, session.getId());

            // Send initial attendance data
            eventService.monitorAttendance(eventId, sessions);

            // Send welcome message
            session.sendMessage(new TextMessage("Connected to event: " + eventId));
        } else {
            session.close(CloseStatus.BAD_DATA.withReason("Invalid event ID"));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String eventId = sessionEventMap.get(session.getId());
        log.info("Received message from session {}: {}", session.getId(), message.getPayload());

        // Handle different message types if needed
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();

            // You can add custom message handling here
            if ("REFRESH".equals(payload)) {
                eventService.monitorAttendance(eventId, sessions);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Transport error in session {}: {}", session.getId(), exception.getMessage());
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed for session: {} with status: {}", session.getId(), closeStatus);
        cleanupSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void cleanupSession(WebSocketSession session) {
        sessions.remove(session.getId());
        sessionEventMap.remove(session.getId());
    }

    private String extractEventId(URI uri) {
        if (uri == null) return null;

        try {
            String path = uri.getPath();
            log.debug("Extracting event ID from path: {}", path);

            if (path.matches("/ws/events/.+/monitoring")) {
                String[] parts = path.split("/");
                if (parts.length >= 4) {
                    String eventId = parts[3]; // /ws/events/EVENT123/monitoring
                    log.debug("Extracted event ID: {}", eventId);
                    return eventId;
                }
            }

            log.warn("Path does not match expected pattern: {}", path);
            return null;

        } catch (Exception e) {
            log.error("Error extracting event ID from URI: {}", uri, e);
            return null;
        }
    }

    public void broadcastToEvent(String eventId, String message) {
        sessions.entrySet().stream()
                .filter(entry -> eventId.equals(sessionEventMap.get(entry.getKey())))
                .forEach(entry -> {
                    try {
                        entry.getValue().sendMessage(new TextMessage(message));
                    } catch (Exception e) {
                        log.error("Error sending message to session {}: {}", entry.getKey(), e.getMessage());
                        cleanupSession(entry.getValue());
                    }
                });
    }
}
