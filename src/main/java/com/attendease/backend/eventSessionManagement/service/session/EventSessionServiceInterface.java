package com.attendease.backend.eventSessionManagement.service.session;

import com.attendease.backend.eventSessionManagement.dto.EventSessionCreateDTO;
import com.attendease.backend.eventSessionManagement.dto.response.EventSessionResponseDTO;
import com.attendease.backend.model.enums.EventStatus;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface EventSessionServiceInterface {

    EventSessionResponseDTO createEvent(EventSessionCreateDTO createDTO) throws ExecutionException, InterruptedException;

    EventSessionResponseDTO getEventById(String eventId) throws ExecutionException, InterruptedException;

    List<EventSessionResponseDTO> getAllEvents() throws ExecutionException, InterruptedException;

    List<EventSessionResponseDTO> getEventsByStatus(EventStatus status) throws ExecutionException, InterruptedException;

    List<EventSessionResponseDTO> getEventsByDateRange(Date from, Date to) throws ExecutionException, InterruptedException;

    List<EventSessionResponseDTO> getEventsByStatusAndDateRange(EventStatus status, Date from, Date to) throws ExecutionException, InterruptedException;

    void deleteEventById(String eventId) throws ExecutionException, InterruptedException;

    EventSessionResponseDTO updateEvent(String eventId, EventSessionCreateDTO updateDTO) throws ExecutionException, InterruptedException;

    EventSessionResponseDTO cancelEvent(String eventId) throws ExecutionException, InterruptedException;
}
