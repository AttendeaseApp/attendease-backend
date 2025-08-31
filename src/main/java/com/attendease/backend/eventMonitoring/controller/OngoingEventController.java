package com.attendease.backend.eventMonitoring.controller;

import com.attendease.backend.eventMonitoring.dto.EventSessionsDto;
import com.attendease.backend.eventMonitoring.service.EventService;
import com.attendease.backend.model.events.EventSessions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
