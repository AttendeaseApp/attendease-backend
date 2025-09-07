/**
 * REST Controller for Event Monitoring operations.
 *
 * <b>Base Path:</b> <code>/event-monitoring</code>
 *
 * <b>Endpoints:</b>
 * <ul>
 *   <li><b>GET /event-monitoring/ongoing</b> - List all ongoing events.</li>
 *   <li><b>GET /event-monitoring/{eventId}</b> - Get details of a specific event.</li>
 *   <li><b>WebSocket: /ws/events/{eventId}/monitoring</b> - Real-time attendance monitoring.<br>
 *     <b>How to use:</b> Connect via WebSocket client and send/receive attendance updates.</li>
 * </ul>
 * <b>Responses:</b> JSON objects with event/session details or attendance updates.
 */
package com.attendease.backend.eventMonitoring.controller;

import com.attendease.backend.eventMonitoring.dto.EventSessionsDto;
import com.attendease.backend.eventMonitoring.service.EventService;
import com.attendease.backend.model.events.EventSessions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/event-monitoring")
@CrossOrigin(origins = "*")
public class OngoingEventController {

    private final EventService eventService;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /*
     * Constructor for OngoingEventController.
     */
    public OngoingEventController(EventService eventService) {
        this.eventService = eventService;
    }

    /*
     * Used to fetch all ongoing events.
     * Sample endpoint:
     * GET /event-monitoring/ongoing
     */
    @GetMapping("/ongoing")
    public ResponseEntity<List<EventSessionsDto>> getOngoingEvents() {
        List<EventSessions> ongoingEvents = eventService.getOngoingEvents();
        List<EventSessionsDto> dtos = ongoingEvents.stream().map(EventSessionsDto::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /*
     * Used to fetch details of a specific ongoing event by its ID.
     * Sample endpoint:
     * GET /event-monitoring/{eventId}
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventSessionsDto> getOngoingEventById(@PathVariable String eventId) {
        EventSessions event = eventService.getEventById(eventId);
        EventSessionsDto dto = EventSessionsDto.mapToDto(event);
        return ResponseEntity.ok(dto);
    }

    /*
     * Used for monitoring attendance in real-time via WebSocket.
     *  
     *  Sample WebSocket endpoint:
     *  ws/event-monitoring/ws/{eventId}/monitoring
     */
    @GetMapping("/ws/{eventId}/monitoring")
    public void handleWebSocketConnection(WebSocketSession session, @PathVariable String eventId) {
        sessions.put(session.getId(), session);
        eventService.monitorAttendance(eventId, sessions);
    }
}
